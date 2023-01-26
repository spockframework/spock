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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version number with format major.minor.micro-qualifier.
 */
@Immutable
public final class VersionNumber implements Comparable<VersionNumber> {
  public static final VersionNumber UNKNOWN = new VersionNumber(0, 0, 0, false, null);

  private static final Pattern versionPattern = Pattern.compile("(?<major>\\d+)(?:\\.(?<minor>\\d+))?+(?:\\.(?<micro>\\d+))?+(?<qualifier>[-.].+?)??(?<snapshot>-SNAPSHOT)?");
  private static final String versionTemplate = "%d.%d.%d%s";

  private final int major;
  private final int minor;
  private final int micro;
  private final boolean isSnapshot;
  private final String qualifier;
  private final int versionFields;

  public VersionNumber(int major, int minor, int micro, boolean isSnapshot, @Nullable String qualifier) {
    this(major, minor, micro, isSnapshot, qualifier == null ? null : "-" + qualifier, 3);
  }

  // Visible for testing
  VersionNumber(int major, int minor, int micro, boolean isSnapshot, @Nullable String qualifier, int versionFields) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
    this.isSnapshot = isSnapshot;
    this.qualifier = qualifier;
    this.versionFields = versionFields;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getMicro() {
    return micro;
  }

  public String getQualifier() {
    return qualifier == null ? null : qualifier.substring(1);
  }

  public boolean isSnapshot() {
    return isSnapshot;
  }

  @Override
  public int compareTo(VersionNumber other) {
    if (major != other.major) return major - other.major;
    if (minor != other.minor) return minor - other.minor;
    if (micro != other.micro) return micro - other.micro;
    int qualComp = ObjectUtil.compare(getQualifier(), other.getQualifier());
    if (qualComp != 0) return qualComp;
    return isSnapshot == other.isSnapshot
      ? 0
      : isSnapshot
      ? -1
      : 1;
  }

  public boolean equals(Object other) {
    return other instanceof VersionNumber && compareTo((VersionNumber) other) == 0;
  }

  public int hashCode() {
    int result = major;
    result = 31 * result + minor;
    result = 31 * result + micro;
    result = 31 * result + ObjectUtil.hashCode(getQualifier());
    result = 31 * result + Boolean.hashCode(isSnapshot);
    return result;
  }

  public String toString() {
    return String.format(versionTemplate, major, minor, micro, (qualifier == null ? "" : "-" + getQualifier()) + (isSnapshot ? "-SNAPSHOT" : ""));
  }

  public String toOriginalString() {
    return toOriginalString(true, true);
  }

  public String toOriginalString(boolean includeQualifier, boolean includeSnapshot) {
    StringBuilder sb = new StringBuilder();
    sb.append(major);
    if (versionFields > 1) {
      sb.append('.').append(minor);
      if (versionFields > 2) {
        sb.append('.').append(micro);
      }
    }
    if (includeQualifier && qualifier != null) {
      sb.append(qualifier);
    }
    if (includeSnapshot && isSnapshot) {
      sb.append("-SNAPSHOT");
    }
    return sb.toString();
  }

  public static VersionNumber parse(String versionString) {
    if (versionString == null) return UNKNOWN;
    Matcher m = versionPattern.matcher(versionString);
    if (!m.matches()) return UNKNOWN;

    int major = Integer.parseInt(m.group("major"));
    String minorString = m.group("minor");
    int minor = minorString == null ? 0 : Integer.parseInt(minorString);
    String microString = m.group("micro");
    int micro = microString == null ? 0 : Integer.parseInt(microString);
    String qualifier = m.group("qualifier");
    boolean isSnapshot = m.group("snapshot") != null;
    int versionFields = 1;
    if (minorString != null) {
      versionFields++;
      if (microString != null) {
        versionFields++;
      }
    }

    return new VersionNumber(major, minor, micro, isSnapshot, qualifier, versionFields);
  }
}
