/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import spock.util.environment.OperatingSystem;

import java.nio.file.Path;

public enum ThreadDumpUtilityType {
  JSTACK("jstack"),
  JCMD("jcmd");

  private final String fileName;

  ThreadDumpUtilityType(String fileName) {
    this.fileName = fileName;
  }

  public Path getPath(Path javaHome) {
    String commandFile = OperatingSystem.getCurrent().isWindows() ? fileName + ".exe" : fileName;
    if ("jre".equals(javaHome.getFileName().toString())) {
      return javaHome.resolve("../bin").resolve(commandFile).normalize();
    } else {
      return javaHome.resolve("bin").resolve(commandFile).normalize();
    }
  }
}
