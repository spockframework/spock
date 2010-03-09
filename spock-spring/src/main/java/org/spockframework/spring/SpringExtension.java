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

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.ContextConfiguration;

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.util.NotThreadSafe;

import spock.lang.Shared;

// TODO: spec for behavior in presence of inheritance
@NotThreadSafe
public class SpringExtension implements IGlobalExtension {
  public void visitSpec(SpecInfo spec) {
    if (!spec.getReflection().isAnnotationPresent(ContextConfiguration.class)) return;

    checkNoSharedFieldsInjected(spec);

    if (!handleProfileValues(spec)) return;

    SpringTestContextManager manager = new SpringTestContextManager(spec.getReflection());
    final SpringInterceptor interceptor = new SpringInterceptor(manager);
    
    spec.addListener(new AbstractRunListener() {
      public void error(ErrorInfo error) {
        interceptor.error(error);
      }
    });

    SpecInfo topSpec = spec.getTopSpec();
    topSpec.getSetupSpecMethod().addInterceptor(interceptor);
    topSpec.getSetupMethod().addInterceptor(interceptor);
    topSpec.getCleanupMethod().addInterceptor(interceptor);
    topSpec.getCleanupSpecMethod().addInterceptor(interceptor);
  }

  private void checkNoSharedFieldsInjected(SpecInfo spec) {
    for (FieldInfo field : spec.getAllFields()) {
      if (field.getReflection().isAnnotationPresent(Shared.class)
          && (field.getReflection().isAnnotationPresent(Autowired.class)
          || field.getReflection().isAnnotationPresent(Resource.class)))
        throw new SpringExtensionException(
            "@Shared field '%s' cannot be injected; use an instance field instead").format(field.getName());
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
