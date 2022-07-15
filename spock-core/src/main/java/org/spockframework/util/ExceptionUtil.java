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

package org.spockframework.util;

import java.io.*;

import org.jetbrains.annotations.Contract;

/**
 * @author Peter Niederwieser
 */
public class ExceptionUtil {
  /**
   * Allows to throw an unchecked exception without declaring it in a throws clause.
   * The given {@code Throwable} will be unconditionally thrown.
   * The return type is only present to be able to use the method as
   * last statement in a method or lambda that needs a value returned,
   * but the method will never return successfully.
   *
   * @param throwable the throwable to be thrown
   * @param <R>       the fake return type
   * @param <T>       the class of the throwable that will be thrown
   * @return nothing as the method will never return successfully
   * @throws T unconditionally
   */
  @SuppressWarnings("unchecked")
  public static <R, T extends Throwable> R sneakyThrow(Throwable t) throws T {
    throw (T) t;
  }

  public static String printStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  @Contract("!null, _ -> fail; _, !null -> fail")
  public static void throwWithSuppressed(@Nullable Throwable a, @Nullable Throwable b) {
    if (a != null) {
      if (b != null) {
        a.addSuppressed(b);
      }
      sneakyThrow(a);
    }
    if (b != null) {
      sneakyThrow(b);
    }
  }

}

