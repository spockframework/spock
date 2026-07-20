/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * Specs annotated with {@code @CompileStatic} must compile even though
 * Spock rewrites their conditions, and must render failed conditions
 * with the recorded values.
 */
class CompileStaticSupport extends EmbeddedSpecification {

  def "comparison conditions on typed variables compile"() {
    when:
    compiler.compileWithImports """
@groovy.transform.CompileStatic
class ASpec extends Specification {
  def "comparisons"() {
    given:
    int x = 42

    expect:
    x > 40
    x >= 42
    x < 43
    x <= 42
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "property access in conditions compiles"() {
    when:
    compiler.compileWithImports """
@groovy.transform.CompileStatic
class ASpec extends Specification {
  def "property access"() {
    given:
    Pogo pogo = new Pogo(name: "Fred")

    expect:
    pogo.name == "Fred"
  }
}

@groovy.transform.CompileStatic
class Pogo {
  String name
}
"""

    then:
    noExceptionThrown()
  }

  def "indexing in conditions compiles"() {
    when:
    compiler.compileWithImports """
@groovy.transform.CompileStatic
class ASpec extends Specification {
  def "indexing"() {
    given:
    List<String> list = ['a']

    expect:
    list[0] == 'a'
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "top-level method call conditions compile"() {
    when:
    compiler.compileWithImports """
@groovy.transform.CompileStatic
class ASpec extends Specification {
  def "method call condition"() {
    given:
    List<String> list = ['a']

    expect:
    list.contains('a')
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "failed comparison condition renders recorded values"() {
    when:
    runner.runWithImports """
@groovy.transform.CompileStatic
class ASpec extends Specification {
  def "comparison"() {
    given:
    int x = 42

    expect:
    x > 43
  }
}
"""

    then:
    ConditionNotSatisfiedError e = thrown()
    e.condition.rendering.trim() == """
x > 43
| |
| false
42
""".trim()
  }
}
