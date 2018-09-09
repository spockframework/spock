/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.jetbrains.annotations.NotNull;
import org.spockframework.mock.*;
import org.spockframework.mock.codegen.Target;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.*;

import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import net.bytebuddy.*;
import net.bytebuddy.description.modifier.*;
import net.bytebuddy.dynamic.Transformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.*;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.sf.cglib.proxy.*;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Some implementation details of this class are inspired from Spring, EasyMock
 * Class Extensions, JMock, Mockito, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 *
 * @author Peter Niederwieser
 */
public class ProxyBasedMockFactory {
  private static final boolean ignoreByteBuddy = Boolean.getBoolean("org.spockframework.mock.ignoreByteBuddy");
  private static final boolean byteBuddyAvailable = ReflectionUtil.isClassAvailable("net.bytebuddy.ByteBuddy");
  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");

  public static ProxyBasedMockFactory INSTANCE = new ProxyBasedMockFactory();

  public Object create(Class<?> mockType, List<Class<?>> additionalInterfaces, @Nullable List<Object> constructorArgs,
      IProxyBasedMockInterceptor mockInterceptor, ClassLoader classLoader, boolean useObjenesis) throws CannotCreateMockException {
    Object proxy;

    if (mockType.isInterface()) {
      proxy = createDynamicProxyMock(mockType, additionalInterfaces, constructorArgs, mockInterceptor, classLoader);
    } else if (byteBuddyAvailable && !ignoreByteBuddy) {
      proxy = ByteBuddyMockFactory.createMock(mockType, additionalInterfaces,
        constructorArgs, mockInterceptor, classLoader, useObjenesis);
    } else if (cglibAvailable) {
      proxy = CglibMockFactory.createMock(mockType, additionalInterfaces,
          constructorArgs, mockInterceptor, classLoader, useObjenesis);
    } else {
      throw new CannotCreateMockException(mockType,
          ". Mocking of non-interface types requires a code generation library. Please put byte-buddy-1.6.4 or cglib-nodep-3.2 or higher on the class path."
      );
    }

    return proxy;
  }

  private Object createDynamicProxyMock(Class<?> mockType, List<Class<?>> additionalInterfaces,
      List<Object> constructorArgs, IProxyBasedMockInterceptor mockInterceptor, ClassLoader classLoader) {
    if (constructorArgs != null) {
      throw new InvalidSpecException("Interface based mocks may not have constructor arguments");
    }
    List<Class<?>> interfaces = new ArrayList<>();
    interfaces.add(mockType);
    interfaces.addAll(additionalInterfaces);
    interfaces.add(ISpockMockObject.class);
    return Proxy.newProxyInstance(
        classLoader,
        interfaces.toArray(new Class<?>[interfaces.size()]),
        new DynamicProxyMockInterceptorAdapter(mockInterceptor)
    );
  }

  // inner class to defer class loading
  private static class ByteBuddyMockFactory {

    private static final TypeCache<TypeCache.SimpleKey> CACHE =
      new TypeCache.WithInlineExpunction<>(TypeCache.Sort.SOFT);
    private static final Class<?> CODEGEN_TARGET_CLASS = Target.class;
    private static final String CODEGEN_PACKAGE = CODEGEN_TARGET_CLASS.getPackage().getName();

    static Object createMock(final Class<?> type,
                             final List<Class<?>> additionalInterfaces,
                             @Nullable List<Object> constructorArgs,
                             IProxyBasedMockInterceptor interceptor,
                             final ClassLoader classLoader,
                             boolean useObjenesis) {

      Class<?> enhancedType = CACHE.findOrInsert(classLoader,
        new TypeCache.SimpleKey(type, additionalInterfaces),
        new Callable<Class<?>>() {
          @Override
          public Class<?> call() throws Exception {
            String typeName = type.getName();
            Class<?> targetClass = type;
            if (shouldLoadIntoCodegenPackage(type)) {
              typeName = CODEGEN_PACKAGE + "." + type.getSimpleName();
              targetClass = CODEGEN_TARGET_CLASS;
            }
            int randomNumber = Math.abs(ThreadLocalRandom.current().nextInt());
            String name = String.format("%s$%s$%d", typeName, "SpockMock", randomNumber);
            ClassLoadingStrategy<ClassLoader> strategy = determineBestClassLoadingStrategy(targetClass);
            return new ByteBuddy()
              .with(TypeValidation.DISABLED) // https://github.com/spockframework/spock/issues/776
              .ignore(none())
              .subclass(type)
              .name(name)
              .implement(additionalInterfaces)
              .implement(ISpockMockObject.class)
              .method(any())
              .intercept(MethodDelegation.withDefaultConfiguration()
                .withBinders(Morph.Binder.install(ByteBuddyInvoker.class))
                .to(ByteBuddyInterceptorAdapter.class))
              .transform(Transformer.ForMethod.withModifiers(SynchronizationState.PLAIN, Visibility.PUBLIC)) // Overridden methods should be public and non-synchronized.
              .implement(ByteBuddyInterceptorAdapter.InterceptorAccess.class)
              .intercept(FieldAccessor.ofField("$spock_interceptor"))
              .defineField("$spock_interceptor", IProxyBasedMockInterceptor.class, Visibility.PRIVATE)
              .make()
              .load(classLoader, strategy)
              .getLoaded();
          }
        }, CACHE);

      Object proxy = MockInstantiator.instantiate(type, enhancedType, constructorArgs, useObjenesis);
      ((ByteBuddyInterceptorAdapter.InterceptorAccess) proxy).$spock_set(interceptor);
      return proxy;
    }

    // This methods and the ones it calls are inspired by similar code in Mockito's SubclassBytecodeGenerator
    private static boolean shouldLoadIntoCodegenPackage(Class<?> type) {
      return isComingFromJDK(type) || isComingFromSignedJar(type) || isComingFromSealedPackage(type);
    }

    private static boolean isComingFromJDK(Class<?> type) {
      // Try to read Implementation-Title entry from manifest which isn't present in every JDK JAR
      return type.getPackage() != null && "Java Runtime Environment".equalsIgnoreCase(type.getPackage().getImplementationTitle())
        || type.getName().startsWith("java.")
        || type.getName().startsWith("javax.");
    }

    private static boolean isComingFromSealedPackage(Class<?> type) {
      return type.getPackage() != null && type.getPackage().isSealed();
    }

    private static boolean isComingFromSignedJar(Class<?> type) {
      return type.getSigners() != null;
    }

    @NotNull
    private static ClassLoadingStrategy<ClassLoader> determineBestClassLoadingStrategy(Class<?> targetClass) throws Exception {
      if (ClassInjector.UsingLookup.isAvailable()) {
        Class<?> methodHandlesClass = Class.forName("java.lang.invoke.MethodHandles");
        Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        Method lookupMethod = methodHandlesClass.getMethod("lookup");
        Method privateLookupInMethod = methodHandlesClass.getMethod("privateLookupIn", Class.class, lookupClass);
        Object lookup = lookupMethod.invoke(null);
        Object privateLookup = privateLookupInMethod.invoke(null, targetClass, lookup);
        return ClassLoadingStrategy.UsingLookup.of(privateLookup);
      }
      if (ClassInjector.UsingReflection.isAvailable()) {
        return ClassLoadingStrategy.Default.INJECTION;
      }
      return ClassLoadingStrategy.Default.WRAPPER;
    }
  }

  // inner class to defer class loading
  private static class CglibMockFactory {
    static Object createMock(Class<?> type, List<Class<?>> additionalInterfaces, @Nullable List<Object> constructorArgs,
        IProxyBasedMockInterceptor interceptor, ClassLoader classLoader, boolean useObjenesis) {
      Enhancer enhancer = new ConstructorFriendlyEnhancer();
      enhancer.setClassLoader(classLoader);
      enhancer.setSuperclass(type);
      List<Class<?>> interfaces = new ArrayList<>(additionalInterfaces);
      interfaces.add(ISpockMockObject.class);
      enhancer.setInterfaces(interfaces.toArray(new Class<?>[interfaces.size()]));
      enhancer.setCallbackFilter(BridgeMethodAwareCallbackFilter.INSTANCE);
      MethodInterceptor cglibInterceptor = new CglibMockInterceptorAdapter(interceptor);
      enhancer.setCallbackTypes(new Class[] {cglibInterceptor.getClass(), NoOp.class});

      Class<?> enhancedType = enhancer.createClass();
      Object proxy = MockInstantiator.instantiate(type, enhancedType, constructorArgs, useObjenesis);
      ((Factory) proxy).setCallbacks(new Callback[] {cglibInterceptor, NoOp.INSTANCE});
      return proxy;
    }

    static class ConstructorFriendlyEnhancer extends Enhancer {
      @Override
      protected void filterConstructors(Class clazz, List constructors) {} // implement all ctors found in superclass
    }

    static class BridgeMethodAwareCallbackFilter implements CallbackFilter {
      // important to use same instance every time; otherwise, CGLIB will
      // keep creating new classes rather than reusing previously generated ones
      static BridgeMethodAwareCallbackFilter INSTANCE = new BridgeMethodAwareCallbackFilter();

      /**
       * @return 0 if the method should be intercepted; 1 otherwise
       */
      @Override
      public int accept(Method method) {
        // All non-bridge methods are intercepted
        if(!method.isBridge()) return 0;

        // Bridge methods are not intercepted unless they override a concrete method in a supertype (issue #122)
        Class[] prmTypes = method.getParameterTypes();
        String methodName = method.getName();
        Class<?> superclass = method.getDeclaringClass().getSuperclass();
        while(superclass != null) {
          for(Method m : superclass.getDeclaredMethods()) {
            if(!methodName.equals(m.getName())) continue;
            if(Arrays.equals(prmTypes, m.getParameterTypes())) {
              int modifiers = m.getModifiers();
              if(Modifier.isAbstract(modifiers)) return 1;
              return (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) ? 0 : 1;
            }
          }
          superclass = superclass.getSuperclass();
        }
        return 1;
      }
    }
  }
}


