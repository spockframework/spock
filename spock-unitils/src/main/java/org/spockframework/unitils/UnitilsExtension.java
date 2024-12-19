/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.unitils;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.util.NotThreadSafe;

import spock.unitils.UnitilsSupport;

@NotThreadSafe
public class UnitilsExtension implements IStatelessAnnotationDrivenExtension<UnitilsSupport> {
  @Override
  public void visitSpecAnnotation(UnitilsSupport unitilsSupport, SpecInfo spec) {
    UnitilsInterceptor interceptor = new UnitilsInterceptor();
    spec.addSetupSpecInterceptor(interceptor);
    spec.addSetupInterceptor(interceptor);
    spec.addCleanupInterceptor(interceptor);
    for (FeatureInfo feature : spec.getFeatures()) {
      feature.addInterceptor(interceptor);
      feature.getFeatureMethod().addInterceptor(interceptor);
    }
  }
}
