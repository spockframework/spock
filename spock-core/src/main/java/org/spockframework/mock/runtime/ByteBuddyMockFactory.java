package org.spockframework.mock.runtime;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.modifier.SynchronizationState;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.Transformer;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import org.jetbrains.annotations.NotNull;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.codegen.Target;
import org.spockframework.util.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.none;

class ByteBuddyMockFactory {

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
      () -> {
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
          .defineField("$spock_interceptor", IProxyBasedMockInterceptor.class, Visibility.PRIVATE, SyntheticState.SYNTHETIC)
          .make()
          .load(classLoader, strategy)
          .getLoaded();
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
