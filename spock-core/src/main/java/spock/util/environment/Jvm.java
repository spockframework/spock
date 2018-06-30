/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.environment;

import org.spockframework.util.VersionNumber;

/**
 * Provides information on the current JVM, such as its Java version.
 */
public class Jvm {
  private final String javaVersion;
  private final String javaSpecVersion;
  private final VersionNumber javaSpecVersionNumber;

  private Jvm() {
    javaVersion = System.getProperty("java.version");
    javaSpecVersion = System.getProperty("java.specification.version");
    javaSpecVersionNumber = VersionNumber.parse(javaSpecVersion);
  }

  /**
   * The Java version, as returned by the {@code java.version} system property.
   * Examples for valid values (for Oracle/OpenJDK) are {@code "1.6.0_22"} and {@code "1.7.0_07"}.
   *
   * @return the Java version, as returned by the {@code java.version} system property
   */
  public String getJavaVersion() {
    return javaVersion;
  }

  /**
   * The Java specification version, as returned by the {@code java.specification.version} system property.
   * Examples for valid values are {@code "1.6"} and {@code "1.7"}.
   *
   * @return the Java specification version, as returned by the {@code java.specification.version} system property
   */
  public String getJavaSpecificationVersion() {
    return javaSpecVersion;
  }

  /**
   * Tells whether the Java version is 5.
   *
   * @return whether the Java version is 5
   */
  public boolean isJava5() {
    return "1.5".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 6.
   *
   * @return whether the Java version is 6
   */
  public boolean isJava6() {
    return "1.6".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 7.
   *
   * @return whether the Java version is 7
   */
  public boolean isJava7() {
    return "1.7".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 8.
   *
   * @return whether the Java version is 8
   */
  public boolean isJava8() {
    return "1.8".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 9.
   *
   * @return whether the Java version is 9
   */
  public boolean isJava9() {
    return "9".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 10.
   *
   * @return whether the Java version is 10
   */
  public boolean isJava10() {
    return "10".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 11.
   *
   * @return whether the Java version is 11
   */
  public boolean isJava11() {
    return "11".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is compatible with Java 5.
   *
   * @return whether the Java version is compatible with Java 5
   */
  public boolean isJava5Compatible() {
    return javaSpecVersionNumber.getMajor() > 1 || javaSpecVersionNumber.getMinor() >= 5;
  }

  /**
   * Tells whether the Java version is compatible with Java 6.
   *
   * @return whether the Java version is compatible with Java 6
   */
  public boolean isJava6Compatible() {
    return javaSpecVersionNumber.getMajor() > 1 || javaSpecVersionNumber.getMinor() >= 6;
  }

  /**
   * Tells whether the Java version is compatible with Java 7.
   *
   * @return whether the Java version is compatible with Java 7
   */
  public boolean isJava7Compatible() {
    return javaSpecVersionNumber.getMajor() > 1 || javaSpecVersionNumber.getMinor() >= 7;
  }

  /**
   * Tells whether the Java version is compatible with Java 8.
   *
   * @return whether the Java version is compatible with Java 8
   */
  public boolean isJava8Compatible() {
    return javaSpecVersionNumber.getMajor() > 1 || javaSpecVersionNumber.getMinor() >= 8;
  }

  /**
   * Tells whether the Java version is compatible with Java 9.
   *
   * @return whether the Java version is compatible with Java 9
   */
  public boolean isJava9Compatible() {
    return javaSpecVersionNumber.getMajor() >= 9;
  }

  /**
   * Tells whether the Java version is compatible with Java 10.
   *
   * @return whether the Java version is compatible with Java 10
   */
  public boolean isJava10Compatible() {
    return javaSpecVersionNumber.getMajor() >= 10;
  }

  /**
   * Tells whether the Java version is compatible with Java 11.
   *
   * @return whether the Java version is compatible with Java 11
   */
  public boolean isJava11Compatible() {
    return javaSpecVersionNumber.getMajor() >= 11;
  }

  /**
   * Returns the current JVM.
   *
   * @return the current JVM
   */
  public static Jvm getCurrent() {
    return new Jvm();
  }
}
