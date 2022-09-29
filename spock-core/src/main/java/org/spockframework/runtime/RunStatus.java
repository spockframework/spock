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

package org.spockframework.runtime;

/**
 *
 * @author Peter Niederwieser
 */
public class RunStatus {
  // actions
  public static final int END = 1;
  public static final int ABORT = 2;

  // scopes
  public static final int ITERATION = 4;
  public static final int FEATURE = 8;
  public static final int SPEC = 16;
  public static final int ALL = 32;

  // run states
  public static final int OK = 0;
  public static final int END_ITERATION = END | ITERATION;
  public static final int END_FEATURE = END | FEATURE;
  public static final int END_SPEC = END | SPEC;
  public static final int END_ALL = END | ALL;
  public static final int ABORT_ITERATION = ABORT | ITERATION;
  public static final int ABORT_FEATURE = ABORT | FEATURE;
  public static final int ABORT_SPEC = ABORT | SPEC;
  public static final int ABORT_ALL = ABORT | ALL;

  public static int action(int status) {
    return status & (END | ABORT);
  }

  public static int scope(int status) {
    return status & (ITERATION | FEATURE | SPEC | ALL);
  }

  /**
   * Combines status1 and status2 by individually maximizing action and scope.
   * Example: combine(END_SPEC, ABORT_FEATURE) == ABORT_SPEC
   */
  public static int combine(int status1, int status2) {
    return Math.max(action(status1), action(status2)) | Math.max(scope(status1), scope(status2));
  }
}
