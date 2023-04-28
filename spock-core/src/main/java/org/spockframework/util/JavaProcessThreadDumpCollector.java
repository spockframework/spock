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

package org.spockframework.util;

import org.spockframework.runtime.SpockException;
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface JavaProcessThreadDumpCollector {

  void appendThreadDumpOfCurrentJvm(StringBuilder builder) throws IOException, InterruptedException;

  static JavaProcessThreadDumpCollector create(ThreadDumpUtilityType utility) {
    try {
      return new FunctionalJavaProcessThreadDumpCollector(utility);
    } catch (Exception e) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(stream));
      System.err.printf("Thread dump capturing is not available: " + stream);
      return NO_OP;
    }
  }

  JavaProcessThreadDumpCollector NO_OP = __ -> {
  };

  class FunctionalJavaProcessThreadDumpCollector implements JavaProcessThreadDumpCollector {

    private static final String JAVA_HOME_SYS_PROP = "java.home";

    private final ThreadDumpUtilityType utility;
    private final List<String> command;

    public FunctionalJavaProcessThreadDumpCollector(ThreadDumpUtilityType utility) {
      long pid = currentProcessId();
      Path utilityPath = getUtilityCommandPath(utility);

      this.utility = utility;
      this.command = utility == ThreadDumpUtilityType.JSTACK
        ? Arrays.asList(utilityPath.toString(), Long.toString(pid))
        : Arrays.asList(utilityPath.toString(), Long.toString(pid), "Thread.print");
    }

    @Override
    public void appendThreadDumpOfCurrentJvm(StringBuilder builder) throws IOException, InterruptedException {
      builder.append("Thread dump of current JVM (").append(utility.name()).append("):\n");
      builder.append("------------------------------")
        .append(TextUtil.repeatChar('-', utility.name().length()))
        .append("\n");

      Process process = new ProcessBuilder(command)
        .redirectErrorStream(true)
        .start();

      captureProcessOutput(process, builder);
      process.waitFor();
    }

    private void captureProcessOutput(Process process, StringBuilder builder) throws IOException {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
          builder.append("\n");
        }
      }
    }

    private static Path getUtilityCommandPath(ThreadDumpUtilityType utility) {
      Path utilityPath = utility.getPath(getJavaHome());
      if (!Files.exists(utilityPath)) {
        throw new SpockException("Could not find requested thread dump capturing utility '" + utility.name() + "' under expected path '" + utilityPath.toString() + "'");
      }
      return utilityPath;
    }

    private static Path getJavaHome() {
      return Optional.ofNullable(System.getProperty(JAVA_HOME_SYS_PROP))
        .map(Paths::get)
        .orElseThrow(() -> new SpockException("Could not determine java home directory, as 'java.home' system property is not set"));
    }

    private static long currentProcessId() {
      try {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(jvmName.split("@")[0]);
      } catch (Exception e) {
        throw new SpockException("Could not determine the current process ID", e);
      }
    }
  }
}
