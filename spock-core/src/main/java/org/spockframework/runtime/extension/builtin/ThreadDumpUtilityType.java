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
