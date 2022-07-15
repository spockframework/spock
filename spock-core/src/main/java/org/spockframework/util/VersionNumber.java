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

import java.util.regex.*;

/**
 * A version number with format major.minor.micro-qualifier.
 */
@Immutable
public final class VersionNumber implements Comparable<VersionNumber> {
  public static final VersionNumber UNKNOWN = new VersionNumber(0, 0, 0, null);

	private static final Pattern versionPattern = Pattern.compile("(\\d+)(?:\\.(\\d+))?+(?:\\.(\\d+))?+(?:[-.](.+))?");
	private static final String versionTemplate = "%d.%d.%d%s";

	private final int major;
	private final int minor;
	private final int micro;
  private final String qualifier;

	public VersionNumber(int major, int minor, int micro, @Nullable String qualifier) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
    this.qualifier = qualifier;
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
    return qualifier;
  }

	@Override
  public int compareTo(VersionNumber other) {
		if (major != other.major) return major - other.major;
		if (minor != other.minor) return minor - other.minor;
		if (micro != other.micro) return micro - other.micro;
    return ObjectUtil.compare(qualifier, other.qualifier);
	}

	public boolean equals(Object other) {
		return other instanceof VersionNumber && compareTo((VersionNumber)other) == 0;
	}

  public int hashCode() {
    int result = major;
    result = 31 * result + minor;
    result = 31 * result + micro;
    result = 31 * result + ObjectUtil.hashCode(qualifier);
    return result;
  }

  public String toString() {
		return String.format(versionTemplate, major, minor, micro, qualifier == null ? "" : "-" + qualifier);
	}

	public static VersionNumber parse(String versionString) {
    if (versionString == null) return UNKNOWN;
		Matcher m = versionPattern.matcher(versionString);
		if (!m.matches()) return UNKNOWN;

        int major = Integer.parseInt(m.group(1));
		String minorString = m.group(2);
        int minor = minorString == null ? 0 : Integer.parseInt(minorString);
		String microString = m.group(3);
        int micro = microString == null ? 0 : Integer.parseInt(microString);
    String qualifier = m.group(4);

		return new VersionNumber(major, minor, micro, qualifier);
	}
}

