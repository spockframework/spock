/*
 * Copyright 2024 the original author or authors.
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
 *
 */

package org.spockframework.smoke.extension

import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.MethodInfo
import spock.lang.Snapshotter
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class SnapshotterSpec extends Specification {
  @TempDir
  Path tmpDir

  def "Snapshotter.Store can store and load correctly (#scenario)"(String value) {
    given:
    def updatingStore = new Snapshotter.Store(specificationContext.currentIteration, tmpDir, true, 'txt', StandardCharsets.UTF_8)

    when:
    updatingStore.saveSnapshot("", value)
    def loaded = updatingStore.loadSnapshot("").orElseThrow { new RuntimeException("Missing Snapshot") }

    then:
    loaded == value

    where:
    [scenario, value] << scenarios()
  }


  def scenarios() {
    [
      ['single line', 'some simple value'],
      ['with newlines at beginning and end', '\nsome simple value\n'],
      ['with newlines at beginning and end and in between', '\nsome\nsimple\nvalue\n'],
      ['with multiple newlines at beginning and end and in between', '\n\nsome\n\n\nsimple\n\n\n\nvalue\n\n']
    ]
  }

  @Unroll("safeName calculation [#iterationIndex]")
  def "safeName calculation"() {
    given:
    def featureInfo = new FeatureInfo().tap {
      name = featureName
      parent = specificationContext.currentSpec
      forceParameterized = iterationIndex > 0
      featureMethod = new MethodInfo().tap {
        reflection = Stub(Method) {
          getName() >> '$spock_feature_0_1'
        }
      }
    }
    def iterationInfo = new IterationInfo(featureInfo, iterationIndex, new Object[0], iterationIndex + 1)

    expect:
    Snapshotter.Store.calculateSafeUniqueName(extension, iterationInfo, snapshotId) == safeName

    where:
    featureName | iterationIndex | extension | snapshotId             | safeName
    'a feature' | 0              | 'txt'     | 'a snapshot'           | 'a_feature-a_snapshot.txt'
    'a feature' | 0              | 'txt'     | ''                     | 'a_feature.txt'
    'a feature' | 0              | 'groovy'  | ''                     | 'a_feature.groovy'
    'a feature' | 1              | 'txt'     | 'a snapshot'           | 'a_feature-a_snapshot-[1].txt'
    'a feature' | 1              | 'txt'     | ''                     | 'a_feature-[1].txt'
    'a' * 300   | 0              | 'txt'     | ''                     | 'a' * 242 + '-0_1.txt'
    'a' * 300   | 100            | 'txt'     | 'a longer snapshot id' | 'a' * 215 + '-a_longer_snapshot_id-0_1-[100].txt'
  }

  def "snapshotId is limited to 100 characters"() {
    when:
    Snapshotter.Store.calculateSafeUniqueName('txt', Stub(IterationInfo), "a" * 101)

    then:
    IllegalArgumentException e = thrown()
    e.message.startsWith("'snapshotId' is too long, only 100 characters are allowed, but was 101: aaaaa")
  }
}
