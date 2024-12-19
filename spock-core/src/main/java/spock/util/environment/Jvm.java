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
   * @since 1.2
   * @return whether the Java version is 10
   */
  public boolean isJava10() {
    return "10".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 11.
   *
   * @since 1.2
   * @return whether the Java version is 11
   */
  public boolean isJava11() {
    return "11".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 12.
   *
   * @since 2.0
   * @return whether the Java version is 12
   */
  public boolean isJava12() {
    return "12".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 13.
   *
   * @since 2.0
   * @return whether the Java version is 13
   */
  public boolean isJava13() {
    return "13".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 14.
   *
   * @since 2.0
   * @return whether the Java version is 14
   */
  public boolean isJava14() {
    return "14".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 15.
   *
   * @since 2.0
   * @return whether the Java version is 15
   */
  public boolean isJava15() {
    return "15".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 16.
   *
   * @since 2.0
   * @return whether the Java version is 16
   */
  public boolean isJava16() {
    return "16".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 17.
   *
   * @since 2.0
   * @return whether the Java version is 17
   */
  public boolean isJava17() {
    return "17".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 18.
   *
   * @since 2.0
   * @return whether the Java version is 18
   */
  public boolean isJava18() {
    return "18".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 19.
   *
   * @since 2.0
   * @return whether the Java version is 19
   */
  public boolean isJava19() {
    return "19".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 20.
   *
   * @since 2.0
   * @return whether the Java version is 20
   */
  public boolean isJava20() {
    return "20".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 21.
   *
   * @since 2.0
   * @return whether the Java version is 21
   */
  public boolean isJava21() {
    return "21".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 22.
   *
   * @since 2.0
   * @return whether the Java version is 22
   */
  public boolean isJava22() {
    return "22".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 23.
   *
   * @since 2.0
   * @return whether the Java version is 23
   */
  public boolean isJava23() {
    return "23".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 24.
   *
   * @since 2.4
   * @return whether the Java version is 24
   */
  public boolean isJava24() {
    return "24".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 25.
   *
   * @since 2.4
   * @return whether the Java version is 25
   */
  public boolean isJava25() {
    return "25".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 26.
   *
   * @since 2.4
   * @return whether the Java version is 26
   */
  public boolean isJava26() {
    return "26".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 27.
   *
   * @since 2.4
   * @return whether the Java version is 27
   */
  public boolean isJava27() {
    return "27".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 28.
   *
   * @since 2.4
   * @return whether the Java version is 28
   */
  public boolean isJava28() {
    return "28".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is 29.
   *
   * @since 2.4
   * @return whether the Java version is 29
   */
  public boolean isJava29() {
    return "29".equals(javaSpecVersion);
  }

  /**
   * Tells whether the Java version is equal to the given major Java version.
   *
   * @since 2.0
   * @param majorJavaVersion major java version (e.g. 8, 12, 17) to check the Java version is equal to
   * @return whether the Java version is equal to the given major Java version
   */
  public boolean isJavaVersion(int majorJavaVersion) {
    if (majorJavaVersion == 8) {
      return isJava8();
    } else {
      return javaSpecVersionNumber.getMajor() == majorJavaVersion;
    }
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
   * @since 1.2
   * @return whether the Java version is compatible with Java 10
   */
  public boolean isJava10Compatible() {
    return javaSpecVersionNumber.getMajor() >= 10;
  }

  /**
   * Tells whether the Java version is compatible with Java 11.
   *
   * @since 1.2
   * @return whether the Java version is compatible with Java 11
   */
  public boolean isJava11Compatible() {
    return javaSpecVersionNumber.getMajor() >= 11;
  }

  /**
   * Tells whether the Java version is compatible with Java 12.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 12
   */
  public boolean isJava12Compatible() {
    return javaSpecVersionNumber.getMajor() >= 12;
  }

  /**
   * Tells whether the Java version is compatible with Java 13.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 13
   */
  public boolean isJava13Compatible() {
    return javaSpecVersionNumber.getMajor() >= 13;
  }

  /**
   * Tells whether the Java version is compatible with Java 14.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 14
   */
  public boolean isJava14Compatible() {
    return javaSpecVersionNumber.getMajor() >= 14;
  }

  /**
   * Tells whether the Java version is compatible with Java 15.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 15
   */
  public boolean isJava15Compatible() {
    return javaSpecVersionNumber.getMajor() >= 15;
  }

  /**
   * Tells whether the Java version is compatible with Java 16.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 16
   */
  public boolean isJava16Compatible() {
    return javaSpecVersionNumber.getMajor() >= 16;
  }

  /**
   * Tells whether the Java version is compatible with Java 17.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 17
   */
  public boolean isJava17Compatible() {
    return javaSpecVersionNumber.getMajor() >= 17;
  }

  /**
   * Tells whether the Java version is compatible with Java 18.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 18
   */
  public boolean isJava18Compatible() {
    return javaSpecVersionNumber.getMajor() >= 18;
  }

  /**
   * Tells whether the Java version is compatible with Java 19.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 19
   */
  public boolean isJava19Compatible() {
    return javaSpecVersionNumber.getMajor() >= 19;
  }

  /**
   * Tells whether the Java version is compatible with Java 20.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 20
   */
  public boolean isJava20Compatible() {
    return javaSpecVersionNumber.getMajor() >= 20;
  }

  /**
   * Tells whether the Java version is compatible with Java 21.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 21
   */
  public boolean isJava21Compatible() {
    return javaSpecVersionNumber.getMajor() >= 21;
  }

  /**
   * Tells whether the Java version is compatible with Java 22.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 22
   */
  public boolean isJava22Compatible() {
    return javaSpecVersionNumber.getMajor() >= 22;
  }

  /**
   * Tells whether the Java version is compatible with Java 23.
   *
   * @since 2.0
   * @return whether the Java version is compatible with Java 23
   */
  public boolean isJava23Compatible() {
    return javaSpecVersionNumber.getMajor() >= 23;
  }

  /**
   * Tells whether the Java version is compatible with Java 24.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 24
   */
  public boolean isJava24Compatible() {
    return javaSpecVersionNumber.getMajor() >= 24;
  }

  /**
   * Tells whether the Java version is compatible with Java 25.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 25
   */
  public boolean isJava25Compatible() {
    return javaSpecVersionNumber.getMajor() >= 25;
  }

  /**
   * Tells whether the Java version is compatible with Java 26.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 26
   */
  public boolean isJava26Compatible() {
    return javaSpecVersionNumber.getMajor() >= 26;
  }

  /**
   * Tells whether the Java version is compatible with Java 27.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 27
   */
  public boolean isJava27Compatible() {
    return javaSpecVersionNumber.getMajor() >= 27;
  }

  /**
   * Tells whether the Java version is compatible with Java 28.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 28
   */
  public boolean isJava28Compatible() {
    return javaSpecVersionNumber.getMajor() >= 28;
  }

  /**
   * Tells whether the Java version is compatible with Java 29.
   *
   * @since 2.4
   * @return whether the Java version is compatible with Java 29
   */
  public boolean isJava29Compatible() {
    return javaSpecVersionNumber.getMajor() >= 29;
  }

  /**
   * Tells whether the Java version is compatible with the given major Java version.
   *
   * @since 2.0
   * @param majorJavaVersion major java version (e.g. 8, 12, 17) to check the Java version compatibility with
   * @return whether the Java version is compatible with the given major Java version
   */
  public boolean isJavaVersionCompatible(int majorJavaVersion) {
    if (majorJavaVersion == 8) {
      return isJava8Compatible();
    } else {
      return javaSpecVersionNumber.getMajor() >= majorJavaVersion;
    }
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
