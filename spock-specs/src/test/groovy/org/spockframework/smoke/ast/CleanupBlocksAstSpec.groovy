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

package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot
import spock.lang.Issue
import spock.util.Show

class CleanupBlocksAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "cleanup rewrite keeps correct method reference"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def foobar = foobar()

  then:
  println(foobar)

  cleanup:
  foobar.size()
}

def foobar() {
  return "foo"
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "cleanup rewrite keeps correct method reference for multi-assignments"() {
    given:
    snapshotter.specBody()
    def snapshotId = (GroovyRuntimeUtil.MAJOR_VERSION >= 5) ? "groovy5" : ""

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def (foobar, b) = foobar()

  then:
  println(foobar)

  cleanup:
  foobar.size()
}

def foobar() {
  return ["foo", "bar"]
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot(snapshotId)
  }
}
