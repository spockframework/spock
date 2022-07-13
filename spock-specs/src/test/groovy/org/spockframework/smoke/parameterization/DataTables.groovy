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

import org.opentest4j.MultipleFailuresError
import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.SpockExecutionException

import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Rollup
import spock.lang.Shared

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

  def "table must have at least two columns"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a
1
    """

    then:
    MultipleFailuresError e = thrown()
    e.failures*.class == [InvalidSpecCompileException] * 2
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

  def "table with just a header are not allowed"() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | b
    """

    then:
    thrown(SpockExecutionException)
  }

  def "table with just a header are not allowed with semicolon"() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a ; b
    """

    then:
    thrown(SpockExecutionException)
  }

  def "header may only contain variable names"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | 1 | b
1 | 1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "header may only contain variable names with semicolon"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a ; 1 ; b
1 ; 1 ; 1
    """

    then:
    MultipleFailuresError e = thrown()
    e.failures*.class == [InvalidSpecCompileException] * 6
  }

  def "header variable names must not clash with local variables"() {
    when:
    compiler.compileFeatureBody """
def local = 1

expect:
true

where:
a | local
1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "header variable names must not clash with local variables with semicolon"() {
    when:
    compiler.compileFeatureBody """
def local = 1

expect:
true

where:
a ; local
1 ; 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

    def "header variable names must not clash with each other"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | a
1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

    def "header variable names must not clash with each other with semicolon"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a ; a
1 ; 1
    """

    then:
    thrown(InvalidSpecCompileException)
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

  @Issue("https://github.com/spockframework/spock/issues/261")
  def "cells cannot reference instance fields"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  true

  where:
  a             | b
  instanceField | 1
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")
  }

  def "cells cannot reference local variables (won't be found)"() {
    when:
    runner.runFeatureBody """
def local = 42

expect:
true

where:
a     | b
local | 1
    """

    then:
    thrown(MissingPropertyException)
  }

  def "cells cannot reference local variables (won't be found) with semicolon"() {
    when:
    runner.runFeatureBody """
def local = 42

expect:
true

where:
a     ; b
local ; 1
    """

    then:
    thrown(MissingPropertyException)
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
      def 'a = #a, b = #b'() {
        expect:
        true

        where:
        a | b
        0 | a + 1
        2 | a
      }
    '''

    then:
    result.testEvents().finished().list().testDescriptor.displayName == ["a = 0, b = 1", "a = 2, b = 2", "a = #a, b = #b" ]
  }

  def "cells can't reference next cells"() {
    when:
    runner.runFeatureBody '''
      expect:
      false

      where:
      a | b
      b | 1
    '''

    then:
    thrown Exception
  }

  def "cells can't reference themselves"() {
    when:
    runner.runFeatureBody '''
      expect:
      false

      where:
      a | b
      1 | b + 1
    '''

    then:
    thrown Exception
  }

  def 'data tables can be referenced from following variables'() {
    expect:
    c == 3

    where:
    a | b
    1 | 2
    1 | a + 1

    c = b + 1
  }

  def "rows must have same number of elements as header"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | b | c
1 | 2 | 3
4 | 5
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "rows must have same number of elements as header with semicolon"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a ; b ; c
1 ; 2 ; 3
4 ; 5
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "cells can be separated with single or double pipe operator"() {
    expect:
    a + b == c

    where:
    a | b || c
    1 | 2 || 3
    4 | 5 || 9
  }

  def "cells can be separated with any amount of semicolons"() {
    expect:
    a + b == c

    where:
    a ; b ;;   c
    1 ; 2 ;;;  3
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
    a | b || c
    1 | 2 || 3
    4 || 5 | 9
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

  def "two or more underscores can not be used to separate multiple data tables if they represent a local variable"() {
    when:
    compiler.compileFeatureBody """
def $dataTableSeparator = ''

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

    then:
    InvalidSpecCompileException isce = thrown()
    isce.message.startsWith('where-blocks may only contain parameterizations')

    where:
    dataTableSeparator << (2..20).collect { '_' * it }
  }

  def "two or more underscores can not be used to separate multiple data tables if they represent a field"() {
    when:
    compiler.compileSpecBody """
def $dataTableSeparator = ''

def foo() {
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
}
"""

    then:
    InvalidSpecCompileException isce = thrown()
    isce.message.startsWith('where-blocks may only contain parameterizations')

    where:
    dataTableSeparator << (2..20).collect { '_' * it }
  }

  def "different tables have to have same row count"() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | _
1 | _
4 | _
3 | _
__

b | _
2 | _
5 | _
"""

    then:
    SpockExecutionException see = thrown()
    see.message =~ 'fewer values than previous'
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

