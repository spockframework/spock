/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring;

import java.lang.annotation.*;
import java.lang.reflect.*;

import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.model.*;
import org.spockframework.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.ContextConfiguration;

import spock.lang.Shared;

@NotThreadSafe
public class SpringExtension extends AbstractGlobalExtension {
  // since Spring 3.2.2
  @SuppressWarnings("unchecked")
  private static final Class<? extends Annotation> contextHierarchyClass =
      (Class) ReflectionUtil.loadClassIfAvailable("org.springframework.test.context.ContextHierarchy");

  // since Spring 4.0
  private static final Method findAnnotationDescriptorForTypesMethod;

  // since Spring-Boot 1.4
  @SuppressWarnings("unchecked")
  private static final Class<? extends Annotation> springBootTestClass =
    (Class) ReflectionUtil.loadClassIfAvailable("org.springframework.boot.test.context.SpringBootTest");

  static {
    Class<?> metaAnnotationUtilsClass =
        ReflectionUtil.loadClassIfAvailable("org.springframework.test.util.MetaAnnotationUtils");
    findAnnotationDescriptorForTypesMethod = metaAnnotationUtilsClass == null ? null :
        ReflectionUtil.getMethodBySignature(metaAnnotationUtilsClass,
            "findAnnotationDescriptorForTypes", Class.class, Class[].class);
  }

  public void visitSpec(SpecInfo spec) {
    if (!isSpringSpec(spec)) return;

    checkNoSharedFieldsInjected(spec);

    if (!handleProfileValues(spec)) return;

    SpringTestContextManager manager = new SpringTestContextManager(spec.getReflection());
    final SpringInterceptor interceptor = new SpringInterceptor(manager);

    spec.addListener(new AbstractRunListener() {
      public void error(ErrorInfo error) {
        interceptor.error(error);
      }
    });

    spec.addSetupSpecInterceptor(interceptor);
    spec.addSetupInterceptor(interceptor);
    spec.addCleanupInterceptor(interceptor);
    spec.addCleanupSpecInterceptor(interceptor);
  }

  private boolean isSpringSpec(SpecInfo spec) {
    boolean contextAnnotationPresent = spec.isAnnotationPresent(ContextConfiguration.class);
    boolean contextHierarchyAnnotationPresent = contextHierarchyClass != null
      && spec.isAnnotationPresent(contextHierarchyClass);
    boolean springBootTestAnnotationPresent = springBootTestClass != null
      && spec.isAnnotationPresent(springBootTestClass);

    boolean springAnnotationPresent = contextAnnotationPresent
      || contextHierarchyAnnotationPresent
      || springBootTestAnnotationPresent;

    if (springAnnotationPresent) {
      return true;
    }

    return findAnnotationDescriptorForTypesMethod != null
        && ReflectionUtil.invokeMethod(
        null, findAnnotationDescriptorForTypesMethod, spec.getReflection(),
        new Class[] {ContextConfiguration.class, contextHierarchyClass}) != null;

  }

  private void checkNoSharedFieldsInjected(SpecInfo spec) {
    for (FieldInfo field : spec.getAllFields()) {
      if (field.getReflection().isAnnotationPresent(Shared.class)
          && (field.getReflection().isAnnotationPresent(Autowired.class)
          // avoid compile-time dependency on optional classes
          || ReflectionUtil.isAnnotationPresent(field.getReflection(), "javax.annotation.Resource")
          || ReflectionUtil.isAnnotationPresent(field.getReflection(), "javax.inject.Inject")))
        throw new SpringExtensionException(
            "@Shared field '%s' cannot be injected; use an instance field instead").withArgs(field.getName());
    }
  }

  private boolean handleProfileValues(SpecInfo spec) {
    if (!ProfileValueUtils.isTestEnabledInThisEnvironment(spec.getReflection())) {
      spec.setExcluded(true);
      return false;
    }

    for (FeatureInfo feature : spec.getAllFeatures())
      if (!ProfileValueUtils.isTestEnabledInThisEnvironment(
          feature.getFeatureMethod().getReflection(), spec.getReflection()))
        feature.setExcluded(true);

    return true;
  }
}
