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

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.util.ReleaseInfo;

import groovy.lang.GroovyObject;

class GroovyReleaseInfo {
  private static VersionNumber version = VersionNumber.parse(determineVersion());

  public static VersionNumber getVersion() {
    return version;
  }

  public static String getArtifactPath() {
    return GroovyObject.class.getProtectionDomain().getCodeSource().getLocation().toString();
  }

  private static String determineVersion() {
    if (invokerHelperGetVersionExists()) return InvokerHelperAccessor.getVersion();
    if (releaseInfoGetVersionExists()) return ReleaseInfoAccessor.getVersion();
    return "0";
  }

  private static boolean invokerHelperGetVersionExists() {
    return Util.isMethodAvailable("org.codehaus.groovy.runtime.InvokerHelper", "getVersion");
  }

  private static boolean releaseInfoGetVersionExists() {
    return Util.isMethodAvailable("org.codehaus.groovy.util.ReleaseInfo", "getVersion");
  }

  private static class InvokerHelperAccessor {
    static String getVersion() {
      return InvokerHelper.getVersion();
    }
  }

  private static class ReleaseInfoAccessor {
    static String getVersion() {
      return ReleaseInfo.getVersion();
    }
  }
}


