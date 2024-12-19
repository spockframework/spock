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

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import spock.lang.IgnoreIf;

import groovy.lang.Closure;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * @author Peter Niederwieser
 */
public class IgnoreIfExtension extends ConditionalExtension<IgnoreIf> implements IStatelessAnnotationDrivenExtension<IgnoreIf> {

  private static final String DEFAULT_MESSAGE = "Ignored via @" + IgnoreIf.class.getSimpleName();

  @Override
  protected Class<? extends Closure> getConditionClass(IgnoreIf annotation) {
    return annotation.value();
  }

  @Override
  protected void specConditionResult(boolean result, IgnoreIf annotation, SpecInfo spec) {
    if (!result) return;
    List<SpecInfo> specsToSkip = annotation.inherited() ? spec.getSpecsCurrentToBottom() : singletonList(spec);
    specsToSkip.forEach(toSkip -> toSkip.skip(ignoredMessage(annotation)));
  }

  @Override
  protected void featureConditionResult(boolean result, IgnoreIf annotation, FeatureInfo feature) {
    if (result) {
      feature.skip(ignoredMessage(annotation));
    }
  }

  @Override
  protected void iterationConditionResult(boolean result, IgnoreIf annotation, IMethodInvocation invocation) {
    if (result) {
      throw new TestAbortedException(ignoredMessage(annotation));
    }
  }

  private static String ignoredMessage(IgnoreIf annotation) {
    String reason = annotation.reason();
    if (reason.isEmpty()) {
      return DEFAULT_MESSAGE;
    } else {
      return DEFAULT_MESSAGE + ": " + reason;
    }
  }
}
