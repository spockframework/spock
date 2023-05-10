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

import org.spockframework.runtime.SpockException;
import spock.util.environment.OperatingSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.spockframework.util.CollectionUtil.listOf;

public enum ThreadDumpUtilityType implements ThreadDumpUtility {

  JSTACK {
    @Override
    String getFileName() {
      return OperatingSystem.getCurrent().isWindows() ? "jstack.exe" : "jstack";
    }

    @Override
    public List<String> getCommand(Path javaHome, long pid) {
      return listOf(getUtilityPath(javaHome).toString(), Long.toString(pid));
    }
  },

  JCMD {
    @Override
    String getFileName() {
      return OperatingSystem.getCurrent().isWindows() ? "jcmd.exe" : "jcmd";
    }

    @Override
    public List<String> getCommand(Path javaHome, long pid) {
      return listOf(getUtilityPath(javaHome).toString(), Long.toString(pid), "Thread.print");
    }
  };

  @Override
  public String getName() {
    return name();
  }

  abstract String getFileName();

  protected Path getUtilityPath(Path javaHome) {
    Path utilityPath;
    if ("jre".equals(javaHome.getFileName().toString())) {
      utilityPath = javaHome.resolve("../bin").resolve(getFileName()).normalize();
    } else {
      utilityPath = javaHome.resolve("bin").resolve(getFileName()).normalize();
    }

    if (!Files.exists(utilityPath)) {
      throw new SpockException("Could not find requested thread dump capturing utility '" + name().toLowerCase() + "' under expected path '" + utilityPath + "'");
    }

    return utilityPath;
  }
}
