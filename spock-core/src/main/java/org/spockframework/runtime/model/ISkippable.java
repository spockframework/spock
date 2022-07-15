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

package org.spockframework.runtime.model;

public interface ISkippable {
  boolean isSkipped();

  /**
   * Execute or skip.
   *
   * @param skipped true if it should be skipped false otherwise
   * @deprecated use {@link #skip(String)} instead
   */
  @Deprecated
  void setSkipped(boolean skipped);

  /**
   * Skip with a skip reason.
   *
   * @param skipReason the reason why it was skipped.
   * @since 2.0
   */
  void skip(String skipReason);

  String getSkipReason();
}
