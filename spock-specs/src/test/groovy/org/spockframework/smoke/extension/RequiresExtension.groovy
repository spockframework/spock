/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.ExtensionException
import spock.lang.FailsWith
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Specification

class RequiresExtension extends EmbeddedSpecification {

  def verifyExecution() {
    when:
    def results = runner.runClass(RequiresExtensionExamples)
    then:
    results.testsSucceededCount == 9
    results.testsFailedCount == 0
    results.testsSkippedCount == 4
    results.testEvents().skipped().list().testDescriptor.displayName == [
      "skips feature if precondition is not satisfied",
      "can skip data providers completely if no data variables are accessed",
      "allows determinate use of multiple filters",
      "feature is ignored if at least one Requires annotation is false"
    ]
  }


  def 'fails directly when referencing an unknown variable'() {
    when:
    runner.runSpecBody """
@Requires({ b })
def foo() {
    expect: false
    where: a = { throw new RuntimeException() }.call()
}
"""

    then:
    ExtensionException ee = thrown()
    ee.cause instanceof MissingPropertyException
  }

  def 'fails directly when throwing an arbitrary exception'() {
    when:
    runner.runSpecBody """
@Requires({ throw new UnsupportedOperationException() })
def foo() {
    expect: false
    where: a = { throw new RuntimeException() }.call()
}
"""

    then:
    ExtensionException ee = thrown()
    ee.cause instanceof UnsupportedOperationException
  }

  def "spec usage with true"() {
    when:
    def result = runner.runWithImports """
@Requires({ 1 < 2 })
class Foo extends Specification {
  def "basic usage"() {
    expect: true
  }
}
"""

    then:
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == 1
  }

  def "spec usage with static class access"() {
    when:
    def result = runner.runWithImports """
@Requires({ shouldRun() })
class Foo extends Specification {
  def "basic usage"() {
    expect: true
  }

  static boolean shouldRun() {
    ${shouldRun}
  }
}
"""

    then:
    result.testsStartedCount == testStartAndSucceededCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == testStartAndSucceededCount
    result.containersSkippedCount == specSkippedCount

    where:
    shouldRun | testStartAndSucceededCount | specSkippedCount
    true      | 1                          | 0
    false     | 0                          | 1
  }

  def "spec usage with false"() {
    when:
    def result = runner.runWithImports """
@Requires({ 1 > 2 })
class Foo extends Specification {
  def "basic usage"() {
    expect: false
  }
}
"""

    then:
    result.testsStartedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == 0
  }

  def "fails if condition cannot be instantiated"() {
    when:
    runner.runWithImports """
class Foo extends Specification {
  @Requires(Bar)
  def "basic usage"() {
    expect: false
  }
}

class Bar extends Closure {
  Bar(Object owner) {
    super(owner)
  }
}
"""

    then:
    ExtensionException ee = thrown()
    ee.message == 'Failed to instantiate condition'
  }

  def "@Requires provides condition access to Specification instance shared fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Shared
  int value
  @Requires({ instance.value != 2 })
  def "bar #input"() {
    value = input

    expect:
    true

    where:
    input << [1, 2, 3]
  }
}
    """)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 3
    result.testsAbortedCount == 1
  }

  def "@Requires provides condition access to Specification instance fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  static int staticValue
  int value

  def setup() {
    value = staticValue
  }

  @Requires({ instance.value != 2 })
  def "bar #input"() {
    staticValue = input

    expect:
    true

    where:
    input << [1, 2, 3]
  }
}
    """)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 3
    result.testsAbortedCount == 1
  }

  def "@Requires provides condition access to static Specification fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  static int value = 1

  @Requires({ value == 1 })
  def "bar"() {
    expect:
    true
  }

  @Requires({ value != 1 })
  def "baz"() {
    expect:
    false
  }
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSkippedCount == 1
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
  }

  static class RequiresExtensionExamples extends Specification {

    @Requires({ 1 < 2 })
    def "runs feature if precondition is satisfied"() {
      expect: true
    }

    @Requires({ 1 > 2 })
    def "skips feature if precondition is not satisfied"() {
      expect: true
    }

    @Requires({ os.windows || os.linux || os.macOs || os.solaris || os.other })
    def "provides OS information"() {
      expect: true
    }

    @Requires({
      jvm.java8 || jvm.java9 || jvm.java10 || jvm.java11 || jvm.java12 || jvm.java13 || jvm.java14 || jvm.java15 || jvm.java16 || jvm.java17 ||
        jvm.isJavaVersion(18)
    })
    def "provides JVM information"() {
      expect: true
    }

    @Requires({ !env.containsKey("FOO_BAR_BAZ") })
    def "provides access to environment variables"() {
      expect: true
    }

    @Requires({ sys.containsKey("java.version") })
    def "provides access to system properties"() {
      expect: true
    }

    @Requires({ it.sys.containsKey("java.version") })
    def "can use closure argument for an easy option to typecast and use IDE support"() {
      expect: true
    }

    @Requires({ a == 2 })
    def 'can evaluate for single iterations if data variables are accessed'() {
      expect:
      a == 2
      where:
      a << [1, 2]
    }

    @Requires({ false })
    def 'can skip data providers completely if no data variables are accessed'() {
      expect: false
      where:
      a = { throw new RuntimeException() }.call()
    }

    @Issue("https://github.com/spockframework/spock/issues/535")
    @IgnoreIf({ true })
    @Requires({ true })
    def "allows determinate use of multiple filters"() {
      expect: false
    }

    @Requires({ false })
    @Requires({ true })
    def "feature is ignored if at least one Requires annotation is false"() {
      expect: false
    }

    @Requires({ true })
    @Requires({ true })
    @FailsWith(ConditionNotSatisfiedError)
    def "feature is not ignored if all Requires annotations are true"() {
      expect: false
    }
  }
}


