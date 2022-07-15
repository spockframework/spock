/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import static java.lang.String.format;

public class VersionChecker {
  //visibility for testing
  static final String DISABLE_GROOVY_VERSION_CHECK_PROPERTY_NAME = "spock.iKnowWhatImDoing.disableGroovyVersionCheck";

  private static final boolean compatibleGroovyVersion = SpockReleaseInfo.isCompatibleGroovyVersion(GroovyReleaseInfo.getVersion());

  public void checkGroovyVersion(String whoIsChecking) {
    if (!isCompatibleGroovyVersion()) {
      if (isVersionCheckDisabled()) {
        System.err.println(format("Executing Spock %s with NOT compatible Groovy version %s due to set %s system property. " +
          "This is unsupported and may result in weird runtime errors!", SpockReleaseInfo.getVersion(),
          GroovyReleaseInfo.getVersion(), DISABLE_GROOVY_VERSION_CHECK_PROPERTY_NAME));
      } else {
        throw new IncompatibleGroovyVersionException(format(
        "The Spock %s cannot execute because Spock %s is not compatible with Groovy %s. For more information (including enforce mode), " +
        "see https://docs.spockframework.org (section 'Known Issues').\n" +
        "Spock artifact: %s\n" +
        "Groovy artifact: %s",
                whoIsChecking, SpockReleaseInfo.getVersion(), GroovyReleaseInfo.getVersion(),
                SpockReleaseInfo.getArtifactPath(), GroovyReleaseInfo.getArtifactPath()));
      }
    }
  }

  //visibility for testing
  boolean isCompatibleGroovyVersion() {
    return compatibleGroovyVersion;
  }

  private boolean isVersionCheckDisabled() {
    return "true".equals(System.getProperty(DISABLE_GROOVY_VERSION_CHECK_PROPERTY_NAME));
  }
}
