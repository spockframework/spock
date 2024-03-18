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

import org.opentest4j.MultipleFailuresError
import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.SpockExecutionException
import spock.lang.Issue
import spock.lang.Snapshot
import spock.lang.Snapshotter

/**
 * @author Peter Niederwieser
 */
class InvalidWhereBlocks extends EmbeddedSpecification {
  def "data variable collides with local variable"() {
    when:
    compiler.compileFeatureBody """
def x = 0

expect:
x == y

where:
x << [1, 2]
y << [1, 2]
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("A variable named 'x' already exists in this scope")
  }

  def "data variable hides (but doesn't collide with) instance field"() {
    when:
    compiler.compileSpecBody """
def x = 1

def foo() {
  expect:
  x == 2

  where:
  x << 2
}
    """

    then:
    noExceptionThrown()
  }

  def "data variable hides (but doesn't collide with) static field"() {
    when:
    compiler.compileSpecBody """
static String x = 1

def foo() {
  expect:
  x == 2

  where:
  x << 2
}
    """

    then:
    noExceptionThrown()
  }

  def "duplicate data variable"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
x << 1
x << 2
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Duplicate declaration of data variable 'x'")
  }

  @Issue("https://github.com/spockframework/spock/issues/285")
  def "duplicate explicit data variable"() {
    when:
    compiler.compileSpecBody """
def foo(x) {
  expect:
  true

  where:
  x << 1
  x << 2
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Duplicate declaration of data variable 'x'")
  }

  @Issue("https://github.com/spockframework/spock/issues/137")
  def "duplicate data variable in multi-parameterization"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
[x, x] << [[1, 1]]
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Duplicate declaration of data variable 'x'")
  }

  @Issue("https://github.com/spockframework/spock/issues/261")
  def "right-hand side of parameterization may not reference instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  x == 1

  where:
  x << $rhs
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")

    where:
    rhs |_
    "[1, instanceField, 2]" |_
    "{ -> { -> [instanceField] }() }()" |_
  }

  def "right-hand side of data variable may not reference instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  x == 1

  where:
  x = $rhs
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")

    where:
    rhs |_
    "instanceField" |_
    "{ -> { -> instanceField }() }()" |_
  }

  def 'referencing a data variable in a data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
x << [1, 2].iterator()
y << x
"""

    then:
    MissingPropertyException e = thrown()
    e.message.startsWith('No such property: x for class: apackage.ASpec')
  }

  def 'referencing a data variable in a closure in a data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
x << [1, 2].iterator()
y << { x }()
"""

    then:
    MissingPropertyException e = thrown()
    e.message.startsWith('No such property: x for class: apackage.ASpec')
  }

  def 'referencing a data table variable in a data table like data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | b
1 | 2
3 | 4

c << [a, 5]
"""

    then:
    MissingPropertyException e = thrown()
    e.message.startsWith('No such property: a for class: apackage.ASpec')
  }

  def 'referencing a data table parameter in a data table cell is not allowed'() {
    when:
    compiler.compileFeatureBody '''
expect:
true

where:
a | b
1 | $spock_p_a
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith('You should not try to use Spock internals')
  }

  def 'referencing a data table parameter in a closure in a data table cell is not allowed'() {
    when:
    compiler.compileFeatureBody '''
expect:
true

where:
a | b
1 | { $spock_p_a }()
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith('You should not try to use Spock internals')
  }

  def 'referencing a data table parameter in a data provider is not allowed'() {
    when:
    runner.runFeatureBody '''
expect:
true

where:
a | _
1 | _

c << $spock_p_a
'''

    then:
    MissingPropertyException e = thrown()
    e.message == 'No such property: $spock_p_a for class: apackage.ASpec'
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
    e.failures*.message.every { it.startsWith('where-blocks may only contain parameterizations') }
  }

  def "table with just a header is not allowed"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | b
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message == 'Data table must have more than just the header row @ line 5, column 1.'
  }

  def "table with just a header in combination is not allowed"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | b
1 | 2
combined:
c | _
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message == 'Data table must have more than just the header row @ line 8, column 1.'
  }

  def "table with just a header is not allowed with semicolon"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a ; b
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message == 'Data table must have more than just the header row @ line 5, column 1.'
  }

  def "table with just a header in combination is not allowed with semicolon"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a ; b
1 ; 2
combined:
c ; _
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message == 'Data table must have more than just the header row @ line 8, column 1.'
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
    InvalidSpecCompileException e = thrown()
    e.message == 'Header of data table may only contain variable names @ line 5, column 5.'
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
    InvalidSpecCompileException e = thrown()
    e.message == 'Header of data table may only contain variable names @ line 5, column 5.'
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
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("A variable named 'local' already exists in this scope")
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
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("A variable named 'local' already exists in this scope")
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
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Duplicate declaration of data variable 'a'")
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
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Duplicate declaration of data variable 'a'")
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
    MissingPropertyException e = thrown()
    e.message == 'No such property: local for class: apackage.ASpec'
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
    MissingPropertyException e = thrown()
    e.message == 'No such property: local for class: apackage.ASpec'
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
    MissingPropertyException e = thrown()
    e.message.startsWith('No such property: b for class: apackage.ASpec')
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
    MissingPropertyException e = thrown()
    e.message.startsWith('No such property: b for class: apackage.ASpec')
  }

  def "rows must have same number of elements as header"() {
    when:
    compiler.compileFeatureBody """
expect:
true
5
where:
a | b | c
1 | 2 | 3
4 | 5
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith('Row in data table has wrong number of elements (2 instead of 3)')
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
    InvalidSpecCompileException e = thrown()
    e.message.startsWith('Row in data table has wrong number of elements (2 instead of 3)')
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

  def 'using combined label outside where block is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      combined:
      true

      expect:
      true

      where:
      a | _
      1 | _
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between data table and derived data variable is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a | _
      1 | _
      combined:
      b = 1
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between derived data variable and data table is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a = 1
      combined:
      b | _
      1 | _
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between data pipe and derived data variable is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a << [1]
      combined:
      b = 1
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between data pipe and derived data variable is not allowed even with separator'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a << [1]
      combined:
      _____
      b = 1
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between data pipe and derived data variable is not allowed even with separator in front'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a << [1]
      _____
      combined:
      b = 1
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between derived data variable and data pipe is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a = 1
      combined:
      b << [1]
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between derived data variable and data pipe is not allowed even with separator'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a = 1
      combined:
      ________
      b << [1]
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label between derived data variable and data pipe is not allowed even with separator in front'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a = 1
      ________
      ________
      combined:
      b << [1]
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def 'using combined label within semicolon data table row is not allowed'(@Snapshot Snapshotter snapshotter) {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a ; combined: b
      1 ; 2
    '''

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
