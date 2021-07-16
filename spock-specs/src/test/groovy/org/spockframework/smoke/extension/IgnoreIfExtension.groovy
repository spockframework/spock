/*
 * Copyright 2009 the original author or authors.
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
import spock.lang.*

class IgnoreIfExtension extends EmbeddedSpecification {
  @IgnoreIf({ 1 < 2 })
  def "basic usage"() {
    expect: false
  }

  @IgnoreIf({ os.windows || os.linux || os.macOs || os.solaris || os.other })
  def "provides OS information"() {
    expect: false
  }

  @IgnoreIf({
    jvm.java8 || jvm.java9 || jvm.java10 || jvm.java11 || jvm.java12 || jvm.java13 || jvm.java14 || jvm.java15 || jvm.java16 || jvm.java17 ||
      jvm.java18 || jvm.java19 || jvm.java20 || jvm.java21 || jvm.java22 || jvm.java23
  })
  def "provides JVM information"() {
    expect: false
  }

  @IgnoreIf({ !env.containsKey("FOOBARBAZ") })
  def "provides access to environment variables"() {
    expect: false
  }

  @IgnoreIf({ !sys.contains("java.version") })
  def "provides access to system properties"() {
    expect: false
  }

  @IgnoreIf({ !it.sys.contains("java.version") })
  def "can use closure argument for an easy option to typecast and use IDE support"() {
    expect: false
  }

  @IgnoreIf({ a == 1 })
  def 'can evaluate for single iterations if data variables are accessed'() {
    expect:
    a == 2
    where:
    a << [1, 2]
  }

  @IgnoreIf({ true })
  def 'can skip data providers completely if no data variables are accessed'() {
    expect: false
    where:
    a = { throw new RuntimeException() }.call()
  }

  def 'fails directly when referencing an unknown variable'() {
    when:
    runner.runSpecBody """
@IgnoreIf({ b })
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
@IgnoreIf({ throw new UnsupportedOperationException() })
def foo() {
    expect: false
    where: a = { throw new RuntimeException() }.call()
}
"""

    then:
    ExtensionException ee = thrown()
    ee.cause instanceof UnsupportedOperationException
  }

  @Issue("https://github.com/spockframework/spock/issues/535")
  @Requires({ false })
  @IgnoreIf({ false })
  def "allows determinate use of multiple filters"() {
    expect: false
  }

  def "spec usage with true"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ 1 < 2 })
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

  def "spec usage with false"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ 1 > 2 })
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


  def "spec usage with unqualified static method access"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ shouldNotRun() })
class Foo extends Specification {
  def "basic usage"() {
    expect: true
  }

  static boolean shouldNotRun() {
    !${shouldRun}
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

  def "spec usage with unqualified static field access"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ shouldNotRun })
class Foo extends Specification {
  def "basic usage"() {
    expect: true
  }

  static boolean shouldNotRun = !${shouldRun}
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


  def "spec usage with shared field access"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ shared.shouldRun })
class Foo extends Specification {
  @Shared
  boolean shouldRun = ${shouldRun}

  def "basic usage"() {
    expect: true
  }
}
"""

    then:
    result.testsStartedCount == testStartAndSucceededCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == testStartAndSucceededCount
    result.containersStartedCount == 2
    result.containersAbortedCount == specAbortedCount

    where:
    shouldRun | testStartAndSucceededCount | specAbortedCount
    false     | 1                          | 0
    true      | 0                          | 1
  }


  def "spec usage with instance field access"() {
    when:
    def result = runner.runWithImports """
@IgnoreIf({ instance.shouldRun })
class Foo extends Specification {
  boolean shouldRun = ${shouldRun}

  def "basic usage"() {
    expect: true
  }
}
"""

    then:
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == testAbortedCount
    result.testsSucceededCount == testSucceededCount

    where:
    shouldRun | testSucceededCount | testAbortedCount
    false     | 1                  | 0
    true      | 0                  | 1
  }

  def "fails if condition cannot be instantiated"() {
    when:
    runner.runWithImports """
class Foo extends Specification {
  @IgnoreIf(Bar)
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

  @IgnoreIf({ true })
  @IgnoreIf({ false })
  def "feature is ignored if at least one IgnoreIf annotation is true"() {
    expect: false
  }

  def "feature is not ignored if all IgnoreIf annotations are false"() {
    when:
    runner.runSpecBody """
@IgnoreIf({ false })
@IgnoreIf({ false })
def foo() {
  expect: false
}
"""

    then:
    thrown(ConditionNotSatisfiedError)
  }

  @IgnoreIf({ a == 1 })
  @IgnoreIf({ false })
  def "feature is ignored if data variable accessing IgnoreIf annotation is true"() {
    expect: false
    where:
    a = 1
  }

  @IgnoreIf({ a == 1 })
  @IgnoreIf({ a != 1 })
  def "feature is ignored if at least one data variable accessing IgnoreIf annotation is true"() {
    expect: false
    where:
    a = 1
  }

  @IgnoreIf({ true })
  @IgnoreIf({ a != 1 })
  def "feature is ignored if non data variable accessing IgnoreIf annotation is true"() {
    expect: false
    where:
    a = 1
  }

  def "@IgnoreIf provides condition access to Specification shared fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Shared
  int value
  @IgnoreIf({ shared.value == 2 })
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

  def "@IgnoreIf provides condition access to Specification instance shared fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Shared
  int value
  @IgnoreIf({ instance.value == 2 })
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

  def "@IgnoreIf provides condition access to Specification instance fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  static int staticValue
  int value

  def setup() {
    value = staticValue
  }

  @IgnoreIf({ instance.value == 2 })
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

  def "@IgnoreIf provides condition access to static Specification fields"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  static int value = 1

  @IgnoreIf({ value == 1 })
  def "bar"() {
    expect:
    false
  }

  @IgnoreIf({ value != 1 })
  def "baz"() {
    expect:
    true
  }
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSkippedCount == 1
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
  }
}
