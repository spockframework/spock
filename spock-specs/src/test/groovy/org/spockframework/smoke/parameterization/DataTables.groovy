/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.SpockExecutionException
import org.spockframework.runtime.StandardStreamsCapturer
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.ResourceLock
import spock.lang.Rollup
import spock.lang.Shared
import spock.lang.Snapshot
import spock.lang.Snapshotter
import spock.lang.Unroll

import static org.spockframework.runtime.model.parallel.Resources.SYSTEM_ERR
import static org.spockframework.runtime.model.parallel.Resources.SYSTEM_OUT

@Rollup
class DataTables extends EmbeddedSpecification {
  static staticField = 42

  @Shared
  def sharedField = 42

  def "basic usage"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    5 | 7 | 7
    3 | 1 | 3
    9 | 9 | 9
  }

  def "basic usage with semicolon"() {
    expect:
    Math.max(a, b) == c

    where:
    a ; b ; c
    5 ; 7 ; 7
    3 ; 1 ; 3
    9 ; 9 ; 9
  }

  @Issue("https://github.com/spockframework/spock/issues/1573")
  def "estimate iterations correctly"() {
    expect:
    specificationContext.currentIteration.estimatedNumIterations == 3

    where:
    a | b | c
    5 | 7 | 7
    3 | 1 | 3
    9 | 9 | 9
  }

  def "can use pseudo-column to enable one-column table"() {
    expect:
    a == 1

    where:
    a | _
    1 | _
  }

  def "can use pseudo-column to enable one-column table with semicolon"() {
    expect:
    a == 1

    where:
    a ; _
    1 ; _
  }

  def "columns can be declared as parameters"(a, String b) {
    expect:
    a == 3
    b == "wow"

    where:
    a | b
    3 | "wow"
  }

  def "pseudo-column can be omitted from parameter list"(a) {
    expect:
    a == 3

    where:
    a | _
    3 | _
  }

  def "pseudo-column can be declared as parameter"(a, _) {
    expect:
    a == 3

    where:
    a | _
    3 | _
  }

  def "tables can be mixed with other parameterizations"() {
    expect:
    [a, b, c, d] == [1, 2, 3, 4]

    where:
    a << [1]
    b | c
    2 | 3
    d = c + 1
  }

  def "tables with semicolon can be mixed with other parameterizations"() {
    expect:
    [a, b, c, d] == [1, 2, 3, 4]

    where:
    a << [1]
    b ; c
    2 ; 3
    d = c + 1
  }

  def "tables with different separators can be mixed"() {
    expect:
    [a, b, c, d] == [1, 2, 3, 4]

    where:
    a | b
    1 | 2

    ; c ; d ;
    ; 3 ; 4 ;
  }

  def "semicolon separated tables can use boolean operators in cells"() {
    expect:
    a
    b

    where:
    a            ; b
    true | false ; true || false
  }

  def "cells may contain arbitrary expressions"() {
    expect:
    a == "oo"
    b.age == 23
    c == 5

    where:
    a           | b                   | c
    "foo"[1..2] | new Person(age: 23) | Math.max(4, 5)
  }

  def "cells may contain arbitrary expressions with semicolon"() {
    expect:
    a == "oo"
    b.age == 23
    c == 5

    where:
    a           ; b                   ; c
    "foo"[1..2] ; new Person(age: 23) ; Math.max(4, 5)
  }

  def "cells can reference shared and static fields"() {
    expect:
    a == 42
    b == 42

    where:
    a           | b
    staticField | sharedField
  }

  def "cells can reference shared and static fields with semicolon"() {
    expect:
    a == 42
    b == 42

    where:
    a           ; b
    staticField ; sharedField
  }

  def 'cells can reference previous cells'() {
    expect:
    [a, b, c] == [0, 1, 2]

    where:
    a | b     | c
    0 | a + 1 | b + 1
  }

  def 'cells can reference previous cells with semicolon'() {
    expect:
    [a, b, c] == [0, 1, 2]

    where:
    a ; b     ; c
    0 ; a + 1 ; b + 1
  }

  @Issue("https://github.com/spockframework/spock/issues/763")
  def "cells in a data table can refer to the current value for a column to the left (plain reference)"(int a, int b, int c) {
    expect:
    Math.max(a, b) == c

    where:
    a  | b  || c
    10 | 20 || b
    5  | 3  || a
    7  | 9  || b
    15 | 11 || a
  }

  def 'cell references are pointing to the current row'() {
    expect:
    b == 1 + a * 2
    c == 3 * b

    where:
    a | b         | c
    0 | 1 + a * 2 | 3 * b
    1 | 1 + a * 2 | 3 * b
    2 | 1 + a * 2 | 3 * b
  }

  def "cell references are evaluated correctly in the method's name"() {
    when:
    def result = runner.runSpecBody '''
      def "a = #a, b = #b"() {
        expect:
        true

        where:
        a | b
        0 | a + 1
        2 | a
      }
    '''

    then:
    result.testEvents().finished().list().testDescriptor.displayName == ["a = 0, b = 1", "a = 2, b = 2", "a = #a, b = #b"]
  }

  def "data tables can be referenced from following variables"() {
    expect:
    c == 3

    where:
    a | b
    1 | 2
    1 | a + 1

    c = b + 1
  }

  def "cells can be separated with single or double pipe operator"() {
    expect:
    a + b == c

    where:
    a | b || c
    1 | 2 || 3
    4 | 5 || 9
  }

  @Issue('https://github.com/spockframework/spock/issues/1062')
  @Unroll
  def "data tables with #separators can be combined"() {
    when:
    def results = runner.runSpecBody """
      def "a feature (#a #b #c)"() {
        expect:
        true

        where:
        a $sepA _
        1 $sepA _
        2 $sepA _
        combined:
        b $sepB _
        3 $sepB _
        combined:
        c $sepA _
        4 $sepA _
        5 $sepA _
        6 $sepA _
      }
    """

    then:
    results.testsStartedCount == 1 + 6
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c)',
      'a feature (1 3 4)',
      'a feature (2 3 4)',
      'a feature (1 3 5)',
      'a feature (2 3 5)',
      'a feature (1 3 6)',
      'a feature (2 3 6)'
    ]

    where:
    sepA | sepB | separators
    '|'  | '|'  | 'pipes'
    ';'  | ';'  | 'semicolons'
    '|'  | ';'  | 'mixed separators'
  }

  def "combined data table columns can use previous data table column values within one table"() {
    expect:
    a == b

    where:
    i << (1..6)

    a | b
    1 | a
    2 | a
    combined:
    c << (1..3)
  }

  def "filter block can exclude first iteration"() {
    when:
    def result = runner.runFeatureBody '''
expect:
i != 1
a == b

where:
i << (1..6)

a | b
1 | a
2 | a
combined:
c << (1..3)

filter:
i != 1
    '''

    then:
    result.testsAbortedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsStartedCount == 6
    result.testsSucceededCount == 6
  }

  def "filter block can exclude last iteration"() {
    when:
    def result = runner.runFeatureBody '''
expect:
i != 6
a == b

where:
i << (1..6)

a | b
1 | a
2 | a
combined:
c << (1..3)

filter:
i != 6
    '''

    then:
    result.testsAbortedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsStartedCount == 6
    result.testsSucceededCount == 6
  }

  def "filter block can not exclude all iterations"() {
    when:
    runner.runFeatureBody '''
expect:
true

where:
i << (1..6)

a | b
1 | a
2 | a
combined:
c << (1..3)

filter:
false
    '''

    then:
    SpockExecutionException e = thrown()
    e.message == 'Data provider has no data'
  }

  @ResourceLock(SYSTEM_OUT)
  @ResourceLock(SYSTEM_ERR)
  def "filtered iterations are logged"(@Snapshot Snapshotter snapshotter) {
    given:
    snapshotter.normalizedWith {
      it.normalize().replaceAll(/(\tat apackage\.ASpec\.a feature\().*(\.groovy:15\))/, '$1script$2')
    }

    and:
    StandardStreamsCapturer outputCapturer = new StandardStreamsCapturer()
    def output = new StringBuilder()
    outputCapturer.addStandardStreamsListener({
      output.append(it.normalize())
    } as IStandardStreamsListener)
    outputCapturer.start()
    outputCapturer.muteStandardStreams()

    and:
    runner.throwFailure = false
    runner.configurationScript {
      runner {
        logFilteredIterations true
      }
    }

    when:
    def result = runner.runFeatureBody '''
expect:
i == 1
a == b

where:
i << (1..6)

a | b
1 | a
2 | a
combined:
c << (1..3)

filter:
i == 1
    '''

    then:
    snapshotter.assertThat(output.toString()).matchesSnapshot()

    cleanup:
    outputCapturer.stop()
  }

  @PendingFeature(reason = 'previous column access across tables does not yet work as expected with cross-multiplication')
  def "combined data table columns can use previous data table column values across tables"() {
    when:
    def results = runner.runFeatureBody '''
      expect:
      true
      a == b
      c == "x$i"

      where:
      x    | _
      'x1' | _
      'x2' | _
      'x3' | _
      'x4' | _
      'x5' | _
      'x6' | _

      i << (1..6)

      a | _
      1 | _
      2 | _
      combined:
      b | _
      a | _
      combined:
      c | _
      x | _
      x | _
      x | _
    '''

    then:
    results.testsSucceededCount == 1 + 6
  }

  def "data tables with different widths can be combined"() {
    when:
    def results = runner.runSpecBody '''
      def "a feature (#a #b #c #d #e)"() {
        expect:
        true

        where:
        a | b
        1 | 'b'
        2 | 'b'
        combined:
        c | d   | e
        3 | 'd' | 'e'
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c #d #e)',
      'a feature (1 b 3 d e)',
      'a feature (2 b 3 d e)'
    ]
  }

  def "data tables starting with table separator can be combined"() {
    when:
    def results = runner.runSpecBody '''
      def "a feature (#a #b #c #d #e)"() {
        expect:
        true

        where:
        a | b
        1 | 'b'
        2 | 'b'
        combined:
        _____________
        c | d   | e
        3 | 'd' | 'e'
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c #d #e)',
      'a feature (1 b 3 d e)',
      'a feature (2 b 3 d e)'
    ]
  }

  def "data tables ending with table separator can be combined"() {
    when:
    def results = runner.runSpecBody '''
      def "a feature (#a #b #c #d #e)"() {
        expect:
        true

        where:
        a | b
        1 | 'b'
        2 | 'b'
        _____________
        combined:
        c | d   | e
        3 | 'd' | 'e'
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c #d #e)',
      'a feature (1 b 3 d e)',
      'a feature (2 b 3 d e)'
    ]
  }

  def "data tables and data pipes can be combined"() {
    when:
    def results = runner.runSpecBody '''
      def "a feature (#a #b #c)"() {
        expect:
        true

        where:
        a | b
        1 | 'b'
        2 | 'b'
        combined:
        c << [3]
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c)',
      'a feature (1 b 3)',
      'a feature (2 b 3)'
    ]
  }

  def "data pipes and data tables can be combined"() {
    when:
    def results = runner.runSpecBody '''
      def "a feature (#a #b #c)"() {
        expect:
        true

        where:
        a << [3]
        combined:
        b | c
        1 | 'b'
        2 | 'b'
      }
    '''

    then:
    results.testsStartedCount == 1 + 2
    results.testEvents().started().list().testDescriptor.displayName == [
      'a feature (#a #b #c)',
      'a feature (3 1 b)',
      'a feature (3 2 b)'
    ]
  }

  def "cells can be separated with any amount of semicolons"() {
    expect:
    a + b == c

    where:
    a ; b ;; c
    1 ; 2 ;;; 3
    4 ; 5 ;;;; 9
  }

  def "tables can be surrounded by borders of semicolons and underscores"() {
    expect:
    a + b == c
    d + f == g

    where:
    ______________
    ; a ; b ;; c ;
    ; 1 ; 2 ;; 3 ;
    ; 4 ; 5 ;; 9 ;
    ______________

    ______________
    ; d | f || g ;
    ; 1 | 2 || 3 ;
    ; 4 | 5 || 9 ;
    ______________
  }

  def "consistent use of cell separators is not enforced"() {
    expect:
    a + b == c

    where:
    a  | b || c
    1  | 2 || 3
    4 || 5  | 9
  }

  def "two or more underscores can be used to separate multiple data tables"() {
    expect:
    runner.runFeatureBody """
expect:
a + b == c
d + e == f

where:
a | b | c
1 | 2 | 3
4 | 5 | 9
$dataTableSeparator
d | e | f
1 | 2 | 3
4 | 5 | 9
"""

    where:
    dataTableSeparator << (2..20).collect { '_' * it }
  }

  def "derived data variables do not break data table previous column references"() {
    expect:
    y == z

    where:
    x = 1

    and:
    y | z | a
    1 | y | y + z
    2 | y | y + z
  }

  def "data pipe variables do not break data table previous column references"() {
    expect:
    y == z

    where:
    x << [1, 2]

    and:
    y | z | a
    3 | y | y + z
    4 | y | y + z
  }

  def "data table previous column references work in closures"() {
    expect:
    x == y
    z == 2 * x

    where:
    x | y       | z
    1 | { x }() | x + y
    2 | { x }() | x + y
  }

  def "data table previous column references work across data tables"() {
    expect:
    x == y
    z == 2 * x
    a == x
    b == x
    c == z

    where:
    x | y       | z
    1 | x       | x + y
    2 | { x }() | x + y

    and:
    f = 1

    and:
    a | b       | c
    1 | { x }() | x + y
    2 | { x }() | x + y
  }

  static class Person {
    def age
  }
}
