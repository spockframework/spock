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

package org.spockframework.util;

import java.io.File;

public class SpockUserHomeUtil {
  public static File getSpockUserHome() {
    String value = System.getProperty("spock.user.home");
    if (value != null) return new File(value);

    value = System.getenv("SPOCK_USER_HOME");
    if (value != null) return new File(value);

    return new File(System.getProperty("user.home"), ".spock");
  }

  public static File getFileInSpockUserHome(Object... relativePath) {
    return new File(getSpockUserHome(), TextUtil.join(File.separator, relativePath));
  }
}
