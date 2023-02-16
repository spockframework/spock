/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockExecutionException
import spock.lang.*
import spock.util.Show

/**
 * @author Peter Niederwieser
 */
class DataProviders extends EmbeddedSpecification {
  def "null value"() {
    when:
    runner.runFeatureBody """
expect: true
where: x << null
    """

    then:
    thrown(SpockExecutionException)
  }

  def "literal"() {
    expect: x == 1
    where: x << 1
  }

  def "empty list"() {
    when:
    runner.runFeatureBody """
expect: true
where: x << []
    """

    then:
    thrown(SpockExecutionException)
  }

  def "single-element list"() {
    expect: x == 1
    where: x << [1]
  }

  def "multi-element list"() {
    expect: [1, 2, 3].contains x
    where: x << [3, 2, 1]
  }

  def "array"() {
    expect: [1, 2, 3].contains x
    where: x << ([3, 2, 1] as int[])
  }

  def "map"() {
    expect: x.value == x.key.toUpperCase()
    where: x << ["vienna":"VIENNA", "tokyo":"TOKYO"]
  }

  def "matcher"() {
    expect: x.startsWith "a"
    where: x << ("a1a2a3" =~ /a./)
  }

  def "string"() {
    expect: "abc".contains x
    where: x << "cba"
  }

  def "iterator"() {
    expect: [1, 2, 3].contains x
    where: x << new MyIterator()
  }

  def "iterable"() {
    expect: [1, 2, 3].contains x
    where: x << new MyIterable()
  }

  def "disguised iterable"() {
    expect: [1, 2, 3].contains x
    where: x << new MyDisguisedIterable()
  }

  def "enumeration"() {
    expect: ["a", "b", "c"].contains x
    where: x << new StringTokenizer("b c a")
  }

  @Issue("https://github.com/spockframework/spock/issues/1287")
  def "data provider with asserting closure produces error rethrower variable in data provider method"() {
    when:
    def result = compiler.transpileFeatureBody('''
      where:
      dataPipe << [{ assert true }]
      dataVariable = null
    ''', EnumSet.of(Show.METHODS))

    then:
    result.source == '''\
public void $spock_feature_0_0(java.lang.Object dataPipe, java.lang.Object dataVariable) {
    try {
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}

public java.lang.Object $spock_feature_0_0prov0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    return [{ ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder1.reset(), 'true', 2, 29, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, 'true', 2, 29, null, $spock_condition_throwable)}
        finally {
        }
    }]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0) {
    java.lang.Object dataPipe = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object dataVariable = ((null) as java.lang.Object)
    return new java.lang.Object[]{ dataPipe , dataVariable }
}'''
  }

  @Issue("https://github.com/spockframework/spock/issues/1287")
  @Rollup
  def "data provider with asserting closure works properly"() {
    when:
    condition()

    then:
    noExceptionThrown()

    where:
    condition << [{ assert true }]
  }

  @Issue("https://github.com/spockframework/spock/issues/1287")
  def "data variable with asserting closure produces error rethrower variable in data processor method"() {
    when:
    def result = compiler.transpileFeatureBody('''
      where:
      dataPipe << [null]
      dataVariable = { assert true }
    ''', EnumSet.of(Show.METHODS))

    then:
    result.source == '''\
public void $spock_feature_0_0(java.lang.Object dataPipe, java.lang.Object dataVariable) {
    try {
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}

public java.lang.Object $spock_feature_0_0prov0() {
    return [null]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    java.lang.Object dataPipe = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object dataVariable = (({ ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder1.reset(), 'true', 3, 31, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, 'true', 3, 31, null, $spock_condition_throwable)}
        finally {
        }
    }) as java.lang.Object)
    return new java.lang.Object[]{ dataPipe , dataVariable }
}'''
  }

  @Issue("https://github.com/spockframework/spock/issues/1287")
  @Rollup
  def "data variable with asserting closure works properly"() {
    when:
    condition()

    then:
    noExceptionThrown()

    where:
    condition = { assert true }
  }

  def "data providers with same number of values"() {
    expect: x == y
    where: x << [1, 2, 3]; y << [1, 2, 3]
  }

  def "data providers with different number of values"() {
    when:
    runner.runFeatureBody """
expect: x == y
where: $providers
    """

    then:
    thrown(SpockExecutionException)

    where:
    providers << [
        "x << [1, 2]; y << [1]",
        "x << [1]; y << [1, 2]",
        "x << [1]; y << [1, 2]; z << [1]",
        "x << [1, 2]; y << [1]; z << [1, 2]"
    ]
  }

  def "data providers one of which has no values"() {
    when:
    runner.runFeatureBody """
expect: true
where:
x << (1..3)
y << []
    """

    then:
    thrown(SpockExecutionException)
  }

  def "different kinds of data providers used together"() {
    expect: a + b + c + d + e == "abcde"
    where:
      a << "a"
      b << ["b"]
      c << ("c" =~ /./)
      d << (["d"] as String[])
      e << "YES".toLowerCase()[1]
  }

  def "computed"() {
    expect: a == b
    where:
    a << {
      def x = 0
      def y = 1
      [x, y]
    }()
    b << [0, 1]
  }

  def 'data pipe consisting of single variable expression has line information in stack trace'() {
    when:
    runner.runFeatureBody '''
expect: true
where: a << b
'''

    then:
    MissingPropertyException mpe = thrown()
    verifyAll(mpe.stackTrace[0]) {
      methodName == 'a feature'
      lineNumber != -1
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1573")
  def "estimate iterations correctly"() {
    expect:
    specificationContext.currentIteration.estimatedNumIterations == 3

    where:
    a << [1, 2, 3]
    b << (1..3)
  }


  @Issue("https://github.com/spockframework/spock/issues/1573")
  def "estimate iterations correctly for Iterators"() {
    expect:
    specificationContext.currentIteration.estimatedNumIterations == -1

    where:
    b << (1..3).iterator()
  }

  @Shared
  def sizeCalls = 0

  def "call size only once"() {
    expect:
    specificationContext.currentIteration.estimatedNumIterations == 3
    sizeCalls == 1

    where:
    b << new Object() {
      def iterator() {
        (1..3).iterator()
      }
      def size() {
        sizeCalls++
        3
      }
    }
  }

  def 'data pipes can be combined'() {
    when:
    def results = runner.runSpecBody '''
      def 'a feature (#a #b)'() {
        expect:
        true

        where:
        a << [3]
        combined:
        b << [1, 2]
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b)',
      'a feature (3 1)',
      'a feature (3 2)'
    ]
  }

  static class MyIterator implements Iterator {
    def elems = [1, 2, 3]

    boolean hasNext() {
      elems.size() > 0
    }

    Object next() {
      elems.pop()
    }

    void remove() {}
  }

  static class MyIterable implements Iterable {
    Iterator iterator() {
      [3, 2, 1].iterator()
    }
  }

// doesn't implement Iterable
  static class MyDisguisedIterable {
    Iterator iterator() {
      [3, 2, 1].iterator()
    }
  }
}
