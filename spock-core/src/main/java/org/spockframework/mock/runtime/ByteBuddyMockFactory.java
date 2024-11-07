/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import groovy.lang.GroovyObject;
import groovy.transform.Internal;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.MethodManifestation;
import net.bytebuddy.description.modifier.SynchronizationState;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.Transformer;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.spockframework.compiler.SpockNames;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.codegen.Target;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;
import static net.bytebuddy.matcher.ElementMatchers.none;

class ByteBuddyMockFactory {
  /**
   * The mask to use to mask out the {@link TypeCache.SimpleKey#hashCode()} to find the {@link #cacheLocks}.
   */
  private static final int CACHE_LOCK_MASK = 0x0F;

  /**
   * The size of the {@link #cacheLocks}.
   */
  private static final int CACHE_LOCK_SIZE = CACHE_LOCK_MASK + 1;

  private static final Class<?> CODEGEN_TARGET_CLASS = Target.class;
  private static final String CODEGEN_PACKAGE = CODEGEN_TARGET_CLASS.getPackage().getName();
  private static final AnnotationDescription INTERNAL_ANNOTATION = AnnotationDescription.Builder.ofType(Internal.class).build();
  private static final Method MOCK_INTERACTION_VALIDATOR_METHOD = requireNonNull(ReflectionUtil.getMethodByName(ISpockMockObject.class, SpockNames.SPOCK_MOCK_INTERATION_VALIDATOR));

  /**
   * This array contains {@link TypeCachingLock} instances, which are used as java monitor locks for
   * {@link TypeCache#findOrInsert(ClassLoader, Object, Callable, Object)}.
   * The {@code cacheLocks} spreads the lock to acquire over multiple locks instead of using a single lock
   * {@code CACHE} for all {@link TypeCache.SimpleKey}s.
   *
   * <p>Note: We can't simply use the mockedType class lock as a lock,
   * because the {@code TypeCache.SimpleKey}, will be the same for different {@code mockTypes + additionalInterfaces}.
   * See the {@code hashCode} implementation of the {@code TypeCache.SimpleKey}, which has {@code Set} semantics.
   */
  private final TypeCachingLock[] cacheLocks;

  private final TypeCache<TypeCache.SimpleKey> CACHE =
    new TypeCache.WithInlineExpunction<>(TypeCache.Sort.SOFT);

  ByteBuddyMockFactory() {
    cacheLocks = new TypeCachingLock[CACHE_LOCK_SIZE];
    for (int i = 0; i < CACHE_LOCK_SIZE; i++) {
      cacheLocks[i] = new TypeCachingLock();
    }
  }

  /**
   * Returns {@code true}, if the mock is considered local
   * <p>
   * A mock is considered local, if all additional interfaces of the mock (including {@link ISpockMockObject}) are
   * loadable by the target class' classloader.
   *
   * @param targetClass The to-be-mocked class
   * @param additionalInterfaces Additional interfaces of the to-be-mocked type
   * @return true, if this is a local mock. Otherwise false
   */
  @VisibleForTesting
  static boolean isLocalMock(Class<?> targetClass, Collection<Class<?>> additionalInterfaces) {
    // Inspired by https://github.com/mockito/mockito/blob/99426415c0ceb30e55216c3934854528c83f410e/mockito-core/src/main/java/org/mockito/internal/creation/bytebuddy/SubclassBytecodeGenerator.java#L165-L166
    ClassLoader cl = new MultipleParentClassLoader.Builder()
      .appendMostSpecific(targetClass)
      .appendMostSpecific(additionalInterfaces)
      .appendMostSpecific(ISpockMockObject.class)
      .build();
    return cl == targetClass.getClassLoader();
  }

  Object createMock(IMockMaker.IMockCreationSettings settings) {
    final Class<?> type = settings.getMockType();
    TypeCache.SimpleKey key = new TypeCache.SimpleKey(type, settings.getAdditionalInterface());
    ClassLoader classLoader = settings.getClassLoader();

    Class<?> enhancedType = CACHE.findOrInsert(classLoader,
      key,
      () -> {
        String typeName = type.getName();
        Class<?> targetClass = type;
        if (shouldLoadIntoCodegenPackage(type)) {
          typeName = CODEGEN_PACKAGE + "." + type.getSimpleName();
          targetClass = CODEGEN_TARGET_CLASS;
        }
        int randomNumber = Math.abs(ThreadLocalRandom.current().nextInt());
        String name = String.format("%s$%s$%d", typeName, "SpockMock", randomNumber);
        ClassLoadingStrategy<ClassLoader> strategy = determineBestClassLoadingStrategy(targetClass, settings);
        //noinspection resource
        return new ByteBuddy()
          .with(TypeValidation.DISABLED) // https://github.com/spockframework/spock/issues/776
          .ignore(none())
          .subclass(type)
          .name(name)
          .implement(settings.getAdditionalInterface())
          .implement(ISpockMockObject.class)
          .method(m -> isGroovyMOPMethod(type, m))
          .intercept(mockInterceptor())
          .transform(mockTransformer())
          .annotateMethod(INTERNAL_ANNOTATION) //Annotate the Groovy MOP methods with @Internal
          .method(m -> !isGroovyMOPMethod(type, m))
          .intercept(mockInterceptor())
          .transform(mockTransformer())
          .method(m -> m.represents(MOCK_INTERACTION_VALIDATOR_METHOD))
          // Implement the $spock_mockInteractionValidation() method which returns the static field below, so we have a validation instance for every mock class
          .intercept(FixedValue.reference(new ByteBuddyMockInteractionValidator(), SpockNames.SPOCK_MOCK_INTERATION_VALIDATOR))
          .transform(validateMockInteractionTransformer())
          .implement(ByteBuddyInterceptorAdapter.InterceptorAccess.class)
          .intercept(FieldAccessor.ofField("$spock_interceptor"))
          .defineField("$spock_interceptor", IProxyBasedMockInterceptor.class, Visibility.PRIVATE, SyntheticState.SYNTHETIC)
          .make()
          .load(classLoader, strategy)
          .getLoaded();
      }, getCacheLockForKey(key));

    Object proxy = MockInstantiator.instantiate(type, enhancedType, settings.getConstructorArgs(), settings.isUseObjenesis());
    ((ByteBuddyInterceptorAdapter.InterceptorAccess) proxy).$spock_set(settings.getMockInterceptor());
    return proxy;
  }

  private static Transformer<MethodDescription> validateMockInteractionTransformer() {
    return Transformer.ForMethod.withModifiers(SynchronizationState.PLAIN, Visibility.PUBLIC, MethodManifestation.FINAL);
  }

  private static Transformer<MethodDescription> mockTransformer() {
    return Transformer.ForMethod.withModifiers(SynchronizationState.PLAIN, Visibility.PUBLIC); //Overridden methods should be public and non-synchronized.
  }

  private static MethodDelegation mockInterceptor() {
    return MethodDelegation.withDefaultConfiguration()
      .withBinders(Morph.Binder.install(ByteBuddyInvoker.class))
      .to(ByteBuddyInterceptorAdapter.class);
  }

  /**
   * Checks if the passed method, is a Groovy MOP method from {@link GroovyObject}.
   *
   * <p>{@code GroovyObject} defined MOP methods {@code getProperty()}, {@code setProperty()} and {@code invokeMethod()},
   * because these methods are handled in a special way in the {@link AbstractCallSite} when marked with {@link Internal @Internal}.
   * See also {@link GroovyObject} comments.
   * So we need to mark the method with {@link Internal @Internal} annotation, if we intercept it.
   *
   * @param type   the type the mock
   * @param method the method to intercept
   * @return {@code true}, if the method is a Groovy MOP method
   */
  private static boolean isGroovyMOPMethod(Class<?> type, MethodDescription method) {
    return GroovyObject.class.isAssignableFrom(type) &&
      method.getDeclaredAnnotations().isAnnotationPresent(Internal.class) &&
      method.isDefaultMethod();
  }

  /**
   * Returns a {@link TypeCachingLock}, which locks the {@link TypeCache#findOrInsert(ClassLoader, Object, Callable, Object)}.
   *
   * @param key the key to lock
   * @return the {@link TypeCachingLock} to use to lock the {@link TypeCache}
   */
  private TypeCachingLock getCacheLockForKey(TypeCache.SimpleKey key) {
    int hashCode = key.hashCode();
    // Try to spread some higher bits with XOR to lower bits, because we only use lower bits.
    hashCode = hashCode ^ (hashCode >>> 16);
    int index = hashCode & CACHE_LOCK_MASK;
    return cacheLocks[index];
  }

  // This method and the ones it calls are inspired by similar code in Mockito's SubclassBytecodeGenerator
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
  private static ClassLoadingStrategy<ClassLoader> determineBestClassLoadingStrategy(Class<?> targetClass, IMockMaker.IMockCreationSettings settings) throws Exception {
    if (ClassInjector.UsingLookup.isAvailable() && isLocalMock(targetClass, settings.getAdditionalInterface())) {
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

  private static final class TypeCachingLock {
  }
}
