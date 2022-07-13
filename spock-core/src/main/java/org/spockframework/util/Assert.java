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

package org.spockframework.util;

import org.jetbrains.annotations.Contract;

/**
 * Assertions for use within Spock code. Failures indicate internal errors.
 *
 * @author Peter Niederwieser
 */
public abstract class Assert {
  // IDEA: beef up error message ("please submit bug report", include caller in message in case stack trace is lost)
  @Contract("null -> fail")
  public static void notNull(Object obj) {
    notNull(obj, "argument is null");
  }

  @Contract("null, _, _ -> fail")
  public static void notNull(Object obj, String msg, Object... values) {
    if (obj == null) throw new InternalSpockError(String.format(msg, values));
  }

  @Contract("false -> fail")
  public static void that(boolean condition) {
    that(condition, "internal error");
  }

  @Contract("false, _, _ -> fail")
  public static void that(boolean condition, String msg, Object... values) {
    if (!condition) throw new InternalSpockError(String.format(msg, values));
  }

  @Contract("_, _ -> fail")
  public static void fail(String msg, Object... values) {
    that(false, msg, values);
  }
}
