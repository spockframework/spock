/*
 * Copyright 2012 the original author or authors.
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

package spock.util.environment;

import java.util.Locale;

/**
 * Provides information on the current operating system, such as its name, version, and family.
 */
public class OperatingSystem {
  private final String name;
  private final String version;
  private final Family family;

  /**
   * An operating system family.
   */
  public enum Family {
    /**
     * A Linux operating system.
     */
    LINUX,

    /**
     * A Mac OS operating system.
     */
    MAC_OS,

    /**
     * A Windows operating system.
     */
    WINDOWS,

    /**
     * A Solaris operating system.
     */
    SOLARIS,

    /**
     * An operating system other than those listed above.
     */
    OTHER
  }

  private OperatingSystem(String name, String version, Family family) {
    this.name = name;
    this.version = version;
    this.family = family;
  }

  /**
   * Returns the name of the operating system, as returned by the {@code os.name} system property.
   *
   * @return the name of the operating system, as returned by the {@code os.name} system property
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the version of the operating system, as returned by the {@code os.version} system property.
   *
   * @return the version of the operating system, as returned by the {@code os.version} system property
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the family of the operating system.
   *
   * @return the family of the operating system
   */
  public Family getFamily() {
    return family;
  }

  /**
   * Tells whether the operating system family is Linux.
   *
   * @return whether the operating system family is Linux
   */
  public boolean isLinux() {
    return family == Family.LINUX;
  }

  /**
   * Tells whether the operating system family is Mac OS.
   *
   * @return whether the operating system family is Mac OS
   */
  public boolean isMacOs() {
    return family == Family.MAC_OS;
  }

  /**
   * Tells whether the operating system family is Windows.
   *
   * @return whether the operating system family is Windows
   */
  public boolean isWindows() {
    return family == Family.WINDOWS;
  }

  /**
   * Tells whether the operating system family is Solaris.
   *
   * @return whether the operating system family is Solaris
   */
  public boolean isSolaris() {
    return family == Family.SOLARIS;
  }

  /**
   * Tells whether the operating system family is anything other than those listed above.
   *
   * @return whether the operating system family is anything other than those listed above
   */
  public boolean isOther() {
    return family == Family.OTHER;
  }

  /**
   * Returns the current operating system.
   *
   * @return the current operating system
   */
  public static OperatingSystem getCurrent() {
    String name = System.getProperty("os.name");
    String version = System.getProperty("os.version");
    String lowerName = name.toLowerCase(Locale.ROOT);
    if (lowerName.contains("linux")) return new OperatingSystem(name, version, Family.LINUX);
    if (lowerName.contains("mac os") || lowerName.contains("darwin")) return new OperatingSystem(name, version, Family.MAC_OS);
    if (lowerName.contains("windows")) return new OperatingSystem(name, version, Family.WINDOWS);
    if (lowerName.contains("sunos")) return new OperatingSystem(name, version, Family.SOLARIS);
    return new OperatingSystem(name, version, Family.OTHER);
  }
}
