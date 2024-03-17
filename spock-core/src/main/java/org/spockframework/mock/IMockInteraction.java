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

package org.spockframework.mock;

import org.spockframework.util.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * An anticipated interaction between the SUT and one or more mock objects.
 *
 * @author Peter Niederwieser
 */
public interface IMockInteraction {
  int getLine();

  int getColumn();

  String getText();

  boolean matches(IMockInvocation invocation);

  Supplier<Object> accept(IMockInvocation invocation);

  List<IMockInvocation> getAcceptedInvocations();

  int computeSimilarityScore(IMockInvocation invocation);

  String describeMismatch(IMockInvocation invocation);

  boolean isSatisfied();

  boolean isExhausted();

  boolean isRequired();
}
