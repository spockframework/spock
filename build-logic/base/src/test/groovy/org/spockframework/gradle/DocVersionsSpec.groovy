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

import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class DocVersionsSpec extends Specification {
  @TempDir
  FileSystemFixture docs

  def "stable versions are filtered and sorted descending, numerically"() {
    given:
    docs.create {
      ['1.0', '2.0', '2.4', '2.9', '2.10', '2.4-M1', '2.4-RC1', '2.5-SNAPSHOT', 'current', 'latest'].each {
        dir(it) {}
      }
    }

    expect:
    DocVersions.stable(docs.currentPath) == ['2.10', '2.9', '2.4', '2.0', '1.0']
  }

  def "snapshot versions are filtered and the latest is detected"() {
    given:
    docs.create {
      ['1.0', '2.4', '2.4-SNAPSHOT', '2.5-SNAPSHOT'].each { dir(it) {} }
    }

    expect:
    DocVersions.snapshots(docs.currentPath) == ['2.5-SNAPSHOT', '2.4-SNAPSHOT']
    DocVersions.latestSnapshot(docs.currentPath) == '2.5-SNAPSHOT'
    DocVersions.latestStable(docs.currentPath) == '2.4'
  }
}
