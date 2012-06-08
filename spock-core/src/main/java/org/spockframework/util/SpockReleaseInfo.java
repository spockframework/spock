/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.util;

public class SpockReleaseInfo {
  private static final VersionNumber spockVersion = VersionNumber.parse("0.7-groovy-1.8");
  private static final VersionNumber minGroovyVersion = VersionNumber.parse("1.8.0");
  private static final VersionNumber maxGroovyVersion = VersionNumber.parse("1.8.99");

  public static VersionNumber getVersion() {
    return spockVersion;
  }

  public static String getArtifactPath() {
    return SpockReleaseInfo.class.getProtectionDomain().getCodeSource().getLocation().toString();
  }

  public static boolean isCompatibleGroovyVersion(VersionNumber groovyVersion) {
    if (groovyVersion == VersionNumber.UNKNOWN) return true;

    return minGroovyVersion.compareTo(groovyVersion) <= 0
        && maxGroovyVersion.compareTo(groovyVersion) >= 0;
  }
}
