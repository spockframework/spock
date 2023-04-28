package org.spockframework.runtime.extension.builtin;

import spock.util.environment.OperatingSystem;

public enum ThreadDumpCapturingUtil {
  JSTACK("jstack"),
  JCMD("jcmd");

  private final String fileName;

  ThreadDumpCapturingUtil(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName(OperatingSystem os) {
    return os.isWindows() ? fileName + ".exe" : fileName;
  }
}
