/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

public class VersionChecker {
  private static final boolean compatibleGroovyVersion =
      SpockReleaseInfo.isCompatibleGroovyVersion(GroovyReleaseInfo.getVersion());

  public static void checkGroovyVersion(String whoIsChecking) {
    if (!compatibleGroovyVersion) throw new IncompatibleGroovyVersionException(String.format(
"The Spock %s cannot execute because Spock %s is not compatible with Groovy %s. For more information, see http://docs.spockframework.org\n" +
"Spock artifact: %s\n" +
"Groovy artifact: %s",
        whoIsChecking, SpockReleaseInfo.getVersion(), GroovyReleaseInfo.getVersion(),
        SpockReleaseInfo.getArtifactPath(), GroovyReleaseInfo.getArtifactPath()));
  }
}
