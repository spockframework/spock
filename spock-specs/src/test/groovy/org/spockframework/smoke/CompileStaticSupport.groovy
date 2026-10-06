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

import groovy.transform.CompileStatic
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockComparisonFailure

/**
 * Specs annotated with {@code @CompileStatic} must compile even though
 * Spock rewrites their conditions, and must render failed conditions
 * with the recorded values.
 */
class CompileStaticSupport extends EmbeddedSpecification {

  def setup() {
    compiler.addClassImport(CompileStatic)
    runner.addClassImport(CompileStatic)
  }

  def "comparison conditions on typed variables compile"() {
    when:
    compiler.compileWithImports """
@CompileStatic
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
@CompileStatic
class ASpec extends Specification {
  def "property access"() {
    given:
    Pogo pogo = new Pogo(name: "Fred")

    expect:
    pogo.name == "Fred"
  }
}

@CompileStatic
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
@CompileStatic
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
@CompileStatic
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

  def "old() compiles and evaluates"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "old"() {
    given:
    List<String> list = ['a']
    int before = list.size()

    when:
    list.add('b')
    int after = list.size()

    then:
    after == old(before) + 1
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "data table cells referencing previous columns compile"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "previous column reference"(int a, int b) {
    when:
    int expected = a + 1

    then:
    b == expected

    where:
    a | b
    1 | a + 1
    5 | a + 1
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "interactions in mock init closures work"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "mock init closure"() {
    given:
    List<String> list = Mock(List) {
      size() >> 3
    }

    when:
    int size = list.size()

    then:
    size == 3
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "interactions in interaction blocks work"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "interaction block"() {
    given:
    List<String> list = Mock()

    when:
    list.add("x")

    then:
    interaction {
      1 * list.add(_)
    }
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "method calls in conditions compile"() {
    when:
    compiler.compileWithImports """
@CompileStatic
class ASpec extends Specification {
  def "method call conditions"() {
    given:
    List<String> list = ['a', 'b']

    expect:
    list.size() == 2
    !list.isEmpty()
    list.first().toUpperCase() == 'A'
    this.helper(1) == 2
    helper(1) == 2
  }

  int helper(int i) {
    i + 1
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "method-level CompileStatic annotation is honored"() {
    when:
    runner.runWithImports """
class ASpec extends Specification {
  @CompileStatic
  def "statically checked feature"() {
    given:
    List<String> list = ['a']

    expect:
    list.size() == 1
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "exception property access in conditions compiles"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    IllegalStateException e = thrown()
    e.message == "bam"
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "method calls on the delegate of with blocks compile"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "with block"() {
    given:
    List<String> list = ['a', 'b']

    expect:
    with(list) {
      size() == 2
      first() == 'a'
    }
  }
}
"""

    then:
    noExceptionThrown()
  }

  def "failed method call condition renders recorded values"() {
    when:
    runner.runWithImports """
@CompileStatic
class ASpec extends Specification {
  def "method call"() {
    given:
    List<String> list = ['a']

    expect:
    list.size() == 2
  }
}
"""

    then:
    SpockComparisonFailure e = thrown()
    e.condition.rendering.trim() == """
list.size() == 2
|    |      |
[a]  1      false
""".trim()
  }

  def "failed comparison condition renders recorded values"() {
    when:
    runner.runWithImports """
@CompileStatic
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
