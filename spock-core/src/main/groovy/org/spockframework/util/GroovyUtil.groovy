/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util

import org.spockframework.runtime.GroovyRuntimeUtil

class GroovyUtil {
  static void tryAll(Closure... blocks) {
    Exception exception = null

    for (block in blocks) {
      try {
        block()
      } catch (Exception e) {
        if (!exception) {
          exception = e
        }
      }
    }

    if (exception) throw exception
  }

  static void closeQuietly(String closeMethod = "close", Object... objects) {
    GroovyRuntimeUtil.closeQuietly(closeMethod, objects)
  }

  static Map filterNullValues(Map input) {
    input.findAll { key, value -> value != null }
  }
}
