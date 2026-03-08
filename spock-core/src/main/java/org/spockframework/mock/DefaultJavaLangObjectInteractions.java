/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.mock;

import org.spockframework.util.UnreachableCodeError;

import java.util.*;

import static java.util.Arrays.asList;

public class DefaultJavaLangObjectInteractions implements IInteractionScope {
  public static final DefaultJavaLangObjectInteractions INSTANCE = new DefaultJavaLangObjectInteractions();

  private DefaultJavaLangObjectInteractions() {}

  private final List<DefaultInteraction> interactions = asList(
      DefaultEqualsInteraction.INSTANCE, DefaultHashCodeInteraction.INSTANCE,
      DefaultToStringInteraction.INSTANCE, DefaultFinalizeInteraction.INSTANCE,
      // Although not really part of java.lang.Object, we include this as it affects equality in Groovy
      DefaultCompareToInteraction.INSTANCE
  );

  @Override
  public void addInteraction(IMockInteraction interaction) {
    throw new UnreachableCodeError("addInteraction");
  }

  @Override
  public void addOrderingBarrier() {
    throw new UnreachableCodeError("addOrderingBarrier()");
  }

  @Override
  public void addUnmatchedInvocation(IMockInvocation invocation) {
    throw new UnreachableCodeError("addUnmatchedInvocation()");
  }

  @Override
  public IMockInteraction match(IMockInvocation invocation) {
    for (IMockInteraction interaction : interactions) {
      if (interaction.matches(invocation)) return interaction;
    }
    return null;
  }

  @Override
  public void verifyInteractions() {
    throw new UnreachableCodeError("verifyInteractions");
  }
}
