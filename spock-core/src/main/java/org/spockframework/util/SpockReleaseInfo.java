/*
 * Copyright 2009 the original author or authors.
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

import java.io.*;
import java.util.Properties;

public class SpockReleaseInfo {
  private static final VersionNumber version;
  private static final VersionNumber minGroovyVersion;
  private static final VersionNumber maxGroovyVersion;

  static {
    Properties properties = new Properties();
    try (InputStream stream = SpockReleaseInfo.class.getResourceAsStream("SpockReleaseInfo.properties")){
      properties.load(stream);
    } catch (IOException e) {
      throw new InternalSpockError("Failed to load `SpockReleaseInfo.properties`", e);
    }
    version = VersionNumber.parse(properties.getProperty("version"));
    minGroovyVersion = VersionNumber.parse(properties.getProperty("minGroovyVersion"));
    maxGroovyVersion = VersionNumber.parse(properties.getProperty("maxGroovyVersion"));
  }

  public static VersionNumber getVersion() {
    return version;
  }

  public static VersionNumber getMinGroovyVersion() {
    return minGroovyVersion;
  }

  public static VersionNumber getMaxGroovyVersion() {
    return maxGroovyVersion;
  }

  public static boolean isCompatibleGroovyVersion(VersionNumber groovyVersion) {
    if (
        // happens when running tests from IDE as the latter doesn't have processed properties file
        minGroovyVersion.equals(VersionNumber.UNKNOWN) ||
        maxGroovyVersion.equals(VersionNumber.UNKNOWN) ||
        // may happen if (future) Groovy version cannot be parsed
        groovyVersion.equals(VersionNumber.UNKNOWN)) {
      return true; // be optimistic
    }

    return minGroovyVersion.compareTo(groovyVersion) <= 0
        && maxGroovyVersion.compareTo(groovyVersion) >= 0;
  }

  public static String getArtifactPath() {
    try {
      return SpockReleaseInfo.class.getProtectionDomain().getCodeSource().getLocation().toString();
    } catch (Exception e) {
      return "unavailable";
    }
  }
}
