/*
 * Copyright 2023 the original author or authors.
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

import spock.lang.Snapshotter
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Path

class SnapshooterSpec extends Specification {
  @TempDir
  Path tmpDir

  def "Snapshotter.Store can store and load correctly (#scenario)"(String value) {
    given:
    def updatingStore = new Snapshotter.Store(specificationContext.currentIteration, tmpDir, true, 'txt', StandardCharsets.UTF_8)

    when:
    updatingStore.saveSnapshot(value)
    def loaded = updatingStore.loadSnapshot().orElseThrow { new RuntimeException("Missing Snapshot") }

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
}
