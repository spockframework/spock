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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.syntax.SyntaxException
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.SpockComparisonFailure
import org.spockframework.runtime.WrongExceptionThrownError
import spock.lang.PendingFeatureIf
import spock.lang.SpockCompileDynamic
import spock.lang.SpockCompileStatic
import spock.lang.SpockTypeChecked

import java.lang.annotation.Annotation

/**
 * Specs annotated with {@code @CompileStatic} must compile even though
 * Spock rewrites their conditions, and must render failed conditions
 * with the recorded values.
 */
class CompileStaticSupport extends EmbeddedSpecification {
  def "comparison conditions on typed variables compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "property access in conditions compiles with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    given:
    compiler.addClassImport(CompileStatic)

    when:
    compiler.compileWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  @PendingFeatureIf(
    value = { GroovyRuntimeUtil.MAJOR_VERSION >= 5 },
    reason = 'https://issues.apache.org/jira/browse/GROOVY-12456'
  )
  def "Map-construction with explicit map syntax compiles in assertions with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    given:
    compiler.addClassImport(CompileStatic)

    when:
    compiler.compileWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "property access"() {
    expect:
    new Pogo([name: "Fred"])
  }
}

@CompileStatic
class Pogo {
  String name
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  @PendingFeatureIf(
    value = { GroovyRuntimeUtil.MAJOR_VERSION >= 5 },
    reason = 'https://issues.apache.org/jira/browse/GROOVY-12456'
  )
  def "Map-construction with implicit map syntax compiles in assertions with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    given:
    compiler.addClassImport(CompileStatic)

    when:
    compiler.compileWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "property access"() {
    expect:
    new Pogo(name: "Fred")
  }
}

@CompileStatic
class Pogo {
  String name
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "@#annotation.simpleName is not allowed on non-specification classes"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
class Pogo {
  String name
}
"""

    then:
    SyntaxException e = thrown()
    e.message.contains('SpockCompileDynamic, SpockCompileStatic, and SpockTypeChecked annotations are not allowed outside Spock specifications, use CompileDynamic, CompileStatic, or TypeChecked instead')

    where:
    annotation << [SpockCompileDynamic, SpockTypeChecked, SpockCompileStatic]
  }

  def "@#annotation.simpleName is not allowed on non-specification methods"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
class Pogo {
  String name
  @${annotation.name}
  def foo() {
  }
}
"""

    then:
    SyntaxException e = thrown()
    e.message.contains('SpockCompileDynamic, SpockCompileStatic, and SpockTypeChecked annotations are not allowed outside Spock specifications, use CompileDynamic, CompileStatic, or TypeChecked instead')

    where:
    annotation << [SpockCompileDynamic, SpockTypeChecked, SpockCompileStatic]
  }

  def "@#annotation.simpleName is not allowed on specification classes"(Class<? extends Annotation> annotation) {
    given:
    compiler.addClassImport(CompileStatic)

    when:
    compiler.compileWithImports """
@${annotation.name}
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
    SyntaxException e = thrown()
    e.message.contains('CompileDynamic, CompileStatic, and TypeChecked annotations are not allowed within Spock specifications as the IDE would majorly misreport type problems, use SpockCompileDynamic, SpockCompileStatic, or SpockTypeChecked instead')

    where:
    annotation << [CompileDynamic, TypeChecked, CompileStatic]
  }

  def "@#annotation.simpleName is not allowed on specification methods"(Class<? extends Annotation> annotation) {
    given:
    compiler.addClassImport(CompileStatic)

    when:
    compiler.compileWithImports """
class ASpec extends Specification {
  @${annotation.name}
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
    SyntaxException e = thrown()
    e.message.contains('CompileDynamic, CompileStatic, and TypeChecked annotations are not allowed within Spock specifications as the IDE would majorly misreport type problems, use SpockCompileDynamic, SpockCompileStatic, or SpockTypeChecked instead')

    where:
    annotation << [CompileDynamic, TypeChecked, CompileStatic]
  }

  def "indexing in conditions compiles with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "top-level method call conditions compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "old() compiles and evaluates with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "data table cells referencing previous columns compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "interactions in mock init closures work with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "non-existent constant-name methods in interactions fail compilation with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "mock init closure"() {
    given:
    List<String> list = Mock(List) { List it ->
      it.does_not_exist() >> 3
    }

    when:
    int size = list.size()

    then:
    size == 0
  }
}
"""

    then:
    CompilationFailedException e = thrown()
    e.message.contains('does_not_exist')

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "non-existent method in where block fails compilation with @#annotation.simpleName on class"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "type-checked where block"() {
    expect:
    true

    where:
    foo = [].does_not_exist()
  }
}
"""

    then:
    CompilationFailedException e = thrown()
    e.message.contains('does_not_exist')

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "non-existent method in where block fails compilation with @#annotation.simpleName on method"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
class ASpec extends Specification {
  @${annotation.name}
  def "type-checked where block"() {
    expect:
    true

    where:
    foo = [].does_not_exist()
  }
}
"""

    then:
    CompilationFailedException e = thrown()
    e.message.contains('does_not_exist')

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "interactions in interaction blocks with argument-inferred variable type work with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "interaction block"() {
    given:
    def list = Mock(List)

    when:
    list.add("x")

    then:
    interaction {
      1 * list.add(*_)
    }
  }
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "interactions in interaction blocks work with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "interaction block"() {
    given:
    List<String> list = Mock()

    when:
    list.add("x")

    then:
    interaction {
      1 * list.add(*_)
    }
  }
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "method calls in conditions compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "method-level CompileStatic annotation is honored with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
class ASpec extends Specification {
  @${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception property access in conditions compiles with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "unclear exception type compiles but fails at runtime with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    def e = thrown(null)
    e.message == "bam"
  }
}
"""

    then:
    InvalidSpecException e = thrown()
    e.message == "Thrown exception type cannot be inferred automatically. Please specify a type explicitly (e.g. 'thrown(MyException)')."

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception property access from inferred variable type compiles with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    def e = thrown(IllegalStateException)
    e.message == "bam"
  }
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception property access with explicit type and null argument compiles with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    IllegalStateException e = thrown(null)
    e.message == "bam"
  }
}
"""

    then:
    noExceptionThrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception assertion with unrelated types should not compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    UnsupportedOperationException e = thrown(IllegalStateException)
    e.message == "bam"
  }
}
"""

    then:
    SyntaxException e = thrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception assertion with compatible types but wrong thrown should fail accordingly with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    RuntimeException e = thrown(UnsupportedOperationException)
    e.message == "bam"
  }
}
"""

    then:
    WrongExceptionThrownError e = thrown()
    e.message == "Expected exception of type 'java.lang.UnsupportedOperationException', but got 'java.lang.IllegalStateException'"

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "exception assertion with incompatible types should not compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    compiler.compileWithImports """
@${annotation.name}
class ASpec extends Specification {
  def "thrown message"() {
    when:
    throw new IllegalStateException("bam")

    then:
    UnsupportedOperationException e = thrown(RuntimeException)
    e.message == "bam"
  }
}
"""

    then:
    SyntaxException e = thrown()

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "method calls on the delegate of with blocks compile with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "failed method call condition renders recorded values with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }

  def "failed comparison condition renders recorded values with @#annotation.simpleName"(Class<? extends Annotation> annotation) {
    when:
    runner.runWithImports """
@${annotation.name}
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

    where:
    annotation << [SpockTypeChecked, SpockCompileStatic]
  }
}
