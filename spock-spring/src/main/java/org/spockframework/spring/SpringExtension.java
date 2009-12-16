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

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.util.NotThreadSafe;

import spock.lang.Shared;

@NotThreadSafe
public class SpringExtension implements IGlobalExtension {
  public void visitSpec(SpecInfo spec) {
    if (!spec.getReflection().isAnnotationPresent(ContextConfiguration.class)) return;

    checkNoSharedFieldInjected(spec);

    TestContextManager manager = new TestContextManager(spec.getReflection());
    SpringInterceptor interceptor = new SpringInterceptor(manager);
    spec.addInterceptor(interceptor);
    spec.getSetupMethod().addInterceptor(interceptor);
    spec.getCleanupMethod().addInterceptor(interceptor);
    for (FeatureInfo feature : spec.getFeatures())
      feature.addInterceptor(interceptor);
  }

  private void checkNoSharedFieldInjected(SpecInfo spec) {
    for (FieldInfo field : spec.getFields()) {
      Field reflection = field.getReflection();
      if (reflection.isAnnotationPresent(Shared.class)
          && (reflection.isAnnotationPresent(Autowired.class)
          || reflection.isAnnotationPresent(Resource.class)))
        throw new SpringExtensionException(
            "@Shared field '%s' cannot be injected; use an instance field instead").format(field.getName());
    }
  }
}
