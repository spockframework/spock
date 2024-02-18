/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.RunContext;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.parallel.ExclusiveResource;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.runtime.model.parallel.Resources;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.SpockDocLinks;
import spock.config.RunnerConfiguration;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.Set;

import groovy.lang.*;

public class GroovyMockFactory implements IMockFactory {

  private static final ExclusiveResource META_CLASS_REGISTRY_RW = new ExclusiveResource(Resources.META_CLASS_REGISTRY,
    ResourceAccessMode.READ_WRITE);
  private static final ExclusiveResource GLOBAL_LOCK = new ExclusiveResource(
    org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_KEY, ResourceAccessMode.READ_WRITE);

  public static final GroovyMockFactory INSTANCE = new GroovyMockFactory();

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.GROOVY;
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) throws CannotCreateMockException {
    final Class<?> type = configuration.getType();
    final MetaClass oldMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    if (oldMetaClass instanceof GroovyMockMetaClass) {
      throw new CannotCreateMockException(type,
        ". The given type is already mocked by Spock.");
    }
    GroovyMockMetaClass newMetaClass = new GroovyMockMetaClass(configuration, specification, oldMetaClass);

    boolean hasAdditionalInterfaces = !configuration.getAdditionalInterfaces().isEmpty();
    if (configuration.isGlobal()) {
      if (!isIsolatedOrHasMetaClassRegistryReadWriteLock(specification)) {
        throw new CannotCreateMockException(type,
          ". Global mocking in parallel execution mode is only possible, when the specification is @Isolated, " +
            "or the specification or feature is annotated with " +
            "@ResourceLock(org.spockframework.runtime.model.parallel.Resources.META_CLASS_REGISTRY).");
      }

      if (type.isInterface()) {
        throw new CannotCreateMockException(type,
            ". Global mocking is only possible for classes, but not for interfaces.");
      }
      if (hasAdditionalInterfaces) {
        throw new CannotCreateMockException(type,
          ". Global cannot add additionalInterfaces.");
      }
      GroovyRuntimeUtil.setMetaClass(type, newMetaClass);
      specification.getSpecificationContext().getCurrentIteration().addCleanup(() -> GroovyRuntimeUtil.setMetaClass(type, oldMetaClass));
      if (isAbstractClass(type)) {
        return createProxyObjectForAbstractGlobalMock(configuration, specification, newMetaClass);
      }
      return MockInstantiator.instantiate(type, type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
    }

    if (isFinalClass(type)) {
      if (hasAdditionalInterfaces) {
        throw new CannotCreateMockException(type,
          ". Cannot add additionalInterfaces to final classes.");
      }
      final Object instance = MockInstantiator.instantiate(type,
          type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
      GroovyRuntimeUtil.setMetaClass(instance, newMetaClass);

      return instance;
    }
    GroovyMockInterceptor mockInterceptor = new GroovyMockInterceptor(configuration, specification, newMetaClass);
    Object proxy = createMockObject(configuration, specification, mockInterceptor);

    if (hasAdditionalInterfaces) {
      //Issue #1405: We need to update the mockMetaClass to reflect the methods of the additional interfaces
      //             The MetaClass of the mock is a bit too much, but we do not have a class representing the hierarchy without the internal Spock interfaces like ISpockMockObject
      MetaClass oldMetaClassOfProxy = GroovyRuntimeUtil.getMetaClass(proxy.getClass());
      GroovyMockMetaClass mockMetaClass = new GroovyMockMetaClass(configuration, specification, oldMetaClassOfProxy);
      mockInterceptor.setMetaClass(mockMetaClass);
    }

    if ((configuration.getNature() == MockNature.SPY) && (configuration.getInstance() != null)) {
      try {
        ReflectionUtil.deepCopyFields(configuration.getInstance(), proxy);
      } catch (Exception e) {
        throw new CannotCreateMockException(type,
          ". Cannot copy fields.\n" + SpockDocLinks.SPY_ON_JAVA_17.getLink(),
          e);
      }
    }
    return proxy;
  }

  private boolean isIsolatedOrHasMetaClassRegistryReadWriteLock(Specification specification) {
    if (!RunContext.get().getConfiguration(RunnerConfiguration.class).parallel.enabled) {
      // we don't have a problem in single threaded execution
      return true;
    }
    FeatureInfo feature = specification.getSpecificationContext().getCurrentIteration().getFeature();
    Set<ExclusiveResource> specExclusiveResources = feature.getSpec().getExclusiveResources();
    if (specExclusiveResources.contains(GLOBAL_LOCK) || specExclusiveResources.contains(META_CLASS_REGISTRY_RW)) {
      return true;
    }
    // GLOBAL_LOCK can't be declared on the feature
    return feature.getExclusiveResources().contains(META_CLASS_REGISTRY_RW);
  }

  private Object createProxyObjectForAbstractGlobalMock(IMockConfiguration configuration, Specification specification, GroovyMockMetaClass mockMetaClass) {
    IProxyBasedMockInterceptor mockInterceptor = new GroovyMockInterceptor(configuration, specification, mockMetaClass);
    //Issue #464: We need to create a subclass mock here to be able to instantiate the abstract class.
    Object proxy = createMockObject(configuration, specification, mockInterceptor);
    //We need also to replace the metaClass of the Proxy Object to ensure correct usage of the mock object.
    GroovyRuntimeUtil.setMetaClass(proxy, mockMetaClass);
    return proxy;
  }

  private Object createMockObject(IMockConfiguration configuration, Specification specification, IProxyBasedMockInterceptor mockInterceptor) {
    IMockMaker.IMockCreationSettings mockCreationSettings = MockCreationSettings.settingsFromMockConfiguration(configuration,
      mockInterceptor,
      specification.getClass().getClassLoader());
    mockCreationSettings.getAdditionalInterface().add(GroovyObject.class);
    return RunContext.get().getMockMakerRegistry().makeMock(mockCreationSettings);
  }

  private boolean isFinalClass(Class<?> type) {
    return !type.isInterface() && Modifier.isFinal(type.getModifiers());
  }

  private boolean isAbstractClass(Class<?> type) {
    return !type.isInterface() && Modifier.isAbstract(type.getModifiers());
  }

  @Override
  public Object createDetached(IMockConfiguration configuration, ClassLoader classLoader) {
    throw new CannotCreateMockException(configuration.getType(),
        ". Detached mocking is only possible for JavaMocks but not GroovyMocks at the moment.");
  }
}
