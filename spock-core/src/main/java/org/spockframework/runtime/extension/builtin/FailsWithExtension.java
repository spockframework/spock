/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import spock.lang.FailsWith;

/**
 * @author Peter Niederwieser
 */
public class FailsWithExtension implements IStatelessAnnotationDrivenExtension<FailsWith> {
  @Override
  public void visitSpecAnnotation(FailsWith failsWith, SpecInfo spec) {
    checkRefersToException(failsWith);

    for (FeatureInfo feature : spec.getFeatures())
      if (!feature.getFeatureMethod().getReflection().isAnnotationPresent(FailsWith.class))
        feature.getFeatureMethod().addInterceptor(new FailsWithInterceptor(failsWith));
  }

  @Override
  public void visitFeatureAnnotation(FailsWith failsWith, FeatureInfo feature) {
    checkRefersToException(failsWith);

    feature.getFeatureMethod().addInterceptor(new FailsWithInterceptor(failsWith));
  }

  @Override
  public void visitFixtureAnnotation(FailsWith failsWith, MethodInfo fixtureMethod) {
    checkRefersToException(failsWith);

    fixtureMethod.addInterceptor(new FailsWithInterceptor(failsWith));
  }

  private void checkRefersToException(FailsWith failsWith) {
    if (Throwable.class.isAssignableFrom(failsWith.value())) return;

    throw new InvalidSpecException("@FailsWith needs to refer to an exception type, " +
        "but does refer to '%s'").withArgs(failsWith.value().getName());
  }
}
