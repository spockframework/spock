/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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
   */
  public static void sneakyThrow(Throwable t) {
    ExceptionUtil.<RuntimeException>doSneakyThrow(t);
  }

  public static String printStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void doSneakyThrow(Throwable t) throws T {
    throw (T) t;
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

