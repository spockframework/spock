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

package org.spockframework.util;

/**
 * Assertions for use within Spock code. Failures indicate internal errors.
 * 
 * @author Peter Niederwieser
 */
public abstract class Assert {
  // IDEA: beef up error message ("please submit bug report", include caller in message in case stack trace is lost)

  public static void notNull(Object obj) {
    notNull(obj, "argument is null");
  }

  public static void notNull(Object obj, String msg) {
    if (obj == null) throw new InternalSpockError(msg);
  }

  public static void that(boolean condition) {
    that(condition, "internal error");
  }

  public static void that(boolean condition, String msg) {
    if (!condition) throw new InternalSpockError(msg);
  }

  public static void fail(String msg) {
    that(false, msg);
  }
}
