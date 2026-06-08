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

package org.spockframework.smoke.ast.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot

class ExternalMockInteractionsAstSpec extends EmbeddedSpecification {

  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  // AST rendering differs between Groovy versions; keep a separate snapshot for Groovy 5+
  private static final String SNAPSHOT_ID = (GroovyRuntimeUtil.MAJOR_VERSION >= 5) ? "groovy5" : ""

  def "MockInteractionSupport class rewrites creation and interactions in place against getSpecification()"() {
    when:
    def result = compiler.transpile('''
import spock.lang.Specification
import spock.mock.MockInteractionSupport

class OrderFixtures implements MockInteractionSupport {
  final Specification specification
  OrderFixtures(Specification specification) { this.specification = specification }

  def createAndStub() {
    def gateway = Mock(List)
    gateway.add("x") >> true
    1 * gateway.add("y")
    return gateway
  }
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot(SNAPSHOT_ID)
  }

  def "@Interactions synthesizes a companion overload and replaces the original body with a throw"() {
    when:
    def result = compiler.transpile('''
import spock.lang.Interactions

class OrderFixtures {
  @Interactions
  void stubHappyPath(List<String> gateway) {
    gateway.add("x") >> true
    1 * gateway.add("y")
  }
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot(SNAPSHOT_ID)
  }

  def "@Interactions call from a spec with a typed receiver is rewritten to pass the spec"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
static class OrderFixtures {
  @spock.lang.Interactions
  void stubHappyPath(List<String> gateway) {
    1 * gateway.add("y")
  }
}

def "a feature"() {
  given:
  List<String> gateway = Mock()
  OrderFixtures fixtures = new OrderFixtures()

  when:
  fixtures.stubHappyPath(gateway)

  then:
  true
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot(SNAPSHOT_ID)
  }

  def "an @Interactions call in a then-block is relocated before the preceding when-block"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
static class OrderFixtures {
  @spock.lang.Interactions
  void expectCharge(List<String> gateway) {
    1 * gateway.add("charge")
  }
}

def "a feature"() {
  given:
  List<String> gateway = Mock()
  OrderFixtures fixtures = new OrderFixtures()

  when:
  gateway.add("charge")

  then:
  fixtures.expectCharge(gateway)
}
''')

    then: "the companion call is moved out of the then-block to before the when-block, like an inline interaction"
    snapshotter.assertThat(result.source).matchesSnapshot(SNAPSHOT_ID)
  }
}
