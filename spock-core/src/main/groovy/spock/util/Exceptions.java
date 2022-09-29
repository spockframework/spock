/*
 * Copyright 2012 the original author or authors.
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
package spock.util;

import org.spockframework.util.Assert;

import java.util.*;

public abstract class Exceptions {
  /**
   * Returns the innermost cause of the specified exception. If the specified exception
   * has no cause, the exception itself is returned.
   *
   * @param exception an exception
   *
   * @return the root cause of the exception
   */
  public static Throwable getRootCause(Throwable exception) {
    Assert.notNull(exception);
    return exception.getCause() == null ? exception : getRootCause(exception.getCause());
  }

  /**
   * Returns a list of all causes of the specified exception. The first element of the
   * returned list is the specified exception itself; the last element is its root cause.
   *
   * @param exception an exception
   *
   * @return the exception's cause chain
   */
  public static List<Throwable> getCauseChain(Throwable exception) {
    Assert.notNull(exception);
    List<Throwable> result = new ArrayList<>();
    collectCauseChain(exception, result);
    return result;
  }

  private static void collectCauseChain(Throwable exception, List<Throwable> collector) {
    if (exception == null) return;
    collector.add(exception);
    collectCauseChain(exception.getCause(), collector);
  }
}
