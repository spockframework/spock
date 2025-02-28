/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.smoke.ast.condition

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot

import java.lang.annotation.Annotation

abstract class BaseVerifyMethodsAstSpec extends EmbeddedSpecification {

  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  abstract Class<? extends Annotation> getAnnotation()

  def "transforms conditions in methods inside spec classes"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody("""
@${annotation.name}
void isPositiveAndEven(int a) {
    a > 0
    a % 2 == 0
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "transforms conditions in methods outside of spec classes"() {
    when:
    def result = compiler.transpile("""
class Assertions {
    @${annotation.name}
    void isPositiveAndEven(int a) {
        a > 0
        a % 2 == 0
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "transforms conditions in private methods"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody("""
@${annotation.name}
private void isPositive(int a) {
    a > 0
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "transforms conditions in static methods"() {
    when:
    def result = compiler.transpile("""
class Assertions {
    @${annotation.name}
    static void isPositive(int a) {
        a > 0
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "transforms explicit conditions"() {
    when:
    def result = compiler.transpile("""
class Assertions {
    @${annotation.name}
    static void isPositiveAndEven(int a) {
        assert a > 0
        assert a % 2 == 0
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "methods without condition declarations stay unchanged"() {
    when:
    def result = compiler.transpile("""
class NonAssertions {
    @${annotation.name}
    static void copy(Integer a) {
        def uselessCopy = a
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "ignores methods without annotation"() {
    when:
    def result = compiler.transpile("""
class Assertions {
    @${annotation.name}
    static void isPositive(int a) {
        a > 0
    }

    static void nonAssertion() {
        println("nonAssertion")
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "transforms only top-level conditions"() {
    when:
    def result = compiler.transpile("""
class Assertions {
    @${annotation.name}
    static void brokenEvenIfPositive(int a) {
        a != 0
        if (a > 0) {
            a % 2 == 0
        }
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "checks for invalid conditions"() {
    when:
    compiler.compile("""
class Assertions {
    @${annotation.name}
    static void assignmentNotCondition(int a) {
        a = 3
    }
}
""")

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("Expected a condition, but found an assignment. Did you intend to write '==' ? @ line 4, column 9")
  }

  def "ignores conditions in overwritten methods"() {
    when:
    def result = compiler.transpile("""
class BaseAssertions {
    @${annotation.name}
    void isValid(int a) {
        a >= 0
    }
}

class SpecificAssertions extends BaseAssertions {
    @Override
    void isValid(int a) {
        a > 0
    }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "fails when verification method has a non-void return value"() {
    when:
    compiler.compile("""
class Assertions {
    @${annotation.name}
    static boolean isPositiveWithReturn(int a) {
        a > 0
        return true
    }
}
""")

    then:
    MultipleCompilationErrorsException e = thrown()
    e.message.contains("Verification helper method 'isPositiveWithReturn' must have a void return type.")
  }

}
