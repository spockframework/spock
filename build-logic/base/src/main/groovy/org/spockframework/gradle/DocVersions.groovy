/*
 *  Copyright 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.gradle

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Path

/**
 * Scans a published {@code docs/} directory for documentation version sub-directories.
 *
 * <p>A "stable" version is a plain {@code X.Y} or {@code X.Y.Z} directory; milestones,
 * release candidates, snapshots and the {@code current}/{@code latest} aliases are ignored.
 * Versions are sorted descending by numeric component (so {@code 2.10} sorts above {@code 2.9}).
 */
@CompileStatic
class DocVersions {
  private static final String STABLE_REGEX = /\d+\.\d+(\.\d+)?/
  private static final String SNAPSHOT_REGEX = /\d+\.\d+(\.\d+)?-SNAPSHOT/

  static List<String> stable(Path docsDir) {
    versionDirs(docsDir)
      .findAll { it ==~ STABLE_REGEX }
      .sort(false) { String a, String b -> compare(b, a) }
  }

  static List<String> snapshots(Path docsDir) {
    versionDirs(docsDir)
      .findAll { it ==~ SNAPSHOT_REGEX }
      .sort(false) { String a, String b -> compare(stripSnapshot(b), stripSnapshot(a)) }
  }

  static String latestStable(Path docsDir) {
    stable(docsDir)[0]
  }

  static String latestSnapshot(Path docsDir) {
    snapshots(docsDir)[0]
  }

  private static List<String> versionDirs(Path docsDir) {
    if (!Files.isDirectory(docsDir)) {
      return []
    }
    List<String> names = []
    docsDir.eachDir { Path dir -> names << dir.fileName.toString() }
    names
  }

  private static String stripSnapshot(String version) {
    version - '-SNAPSHOT'
  }

  private static int compare(String a, String b) {
    List<Integer> pa = a.split(/\./).collect { it.toInteger() }
    List<Integer> pb = b.split(/\./).collect { it.toInteger() }
    int n = Math.max(pa.size(), pb.size())
    for (int i = 0; i < n; i++) {
      int x = i < pa.size() ? pa[i] : 0
      int y = i < pb.size() ? pb[i] : 0
      if (x != y) {
        return x <=> y
      }
    }
    0
  }
}
