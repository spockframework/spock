/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooManyInvocationsError

import java.util.regex.Pattern

class TooManyInvocations extends EmbeddedSpecification {
  static Pattern EMPTY_LINE = Pattern.compile(/^ ++$/, Pattern.MULTILINE)

  def "shows matched invocations, ordered by last occurrence"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)
list.add(1)
list.add(4)
list.add(2)

then:
3 * list.add(_)
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize("""
Too many invocations for:

3 * list.add(_)   (4 invocations)

Matching invocations (ordered by last occurrence):

2 * list.add(2)   <-- this triggered the error
1 * list.add(4)
1 * list.add(1)
    """)
    exceptionMessage == expected
  }

  def "scope-level unmatched invocations are not shown without unsatisfied interactions"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)
list.add(1)

then:
0 * list.add(1)
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * list.add(1)   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "unmatched invocations on different methods are excluded"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.get(0)
list.size()
list.add(1)

then:
0 * list.add(1)
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * list.add(1)   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "wildcard interaction error includes unsatisfied interaction context"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(99)

then:
1 * list.add(42)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(99)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list.add(42)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   99       |  42
            false
    ''')
    exceptionMessage == expected
  }

  def "multiple unsatisfied interactions with wildcard"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(99)

then:
1 * list.add(1)
1 * list.add(2)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(99)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list.add(1)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   99       |  1
            false
1 * list.add(2)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   99       |  2
            false
    ''')
    exceptionMessage == expected
  }

  def "unsatisfied interaction on different mock is not shown"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)
def list2 = Mock(List)

when:
list.add(1)

then:
1 * list2.add(1)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "unsatisfied interaction on different method is not shown"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(1)

then:
1 * list.get(0)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "satisfied interactions are not shown in unsatisfied list"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(1)
list.add(99)

then:
1 * list.add(1)
1 * list.add(42)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(99)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list.add(42)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   99       |  42
            false
    ''')
    exceptionMessage == expected
  }

  def "both unmatched invocations and unsatisfied interactions are shown together"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)
list.add(1)

then:
1 * list.add(42)
0 * list.add(1)
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * list.add(1)   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list.add(42)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   1        |  42
            false
    ''')
    exceptionMessage == expected
  }

  def "no unmatched section when no relevant context exists"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(1)

then:
0 * list.add(1)
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * list.add(1)   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(1)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "casts are printed for mock invocations"() {
    given:
    runner.addClassImport(SomeStatic)

    when:
    runner.runFeatureBody """
GroovyMock(SomeStatic, global: true)

when:
SomeStatic.compile(null as String)

then:
0 * SomeStatic.compile(_)
"""

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    exceptionMessage.contains('<SomeStatic>.compile(null as String)')
  }

  def "partially matched interaction shown as unsatisfied"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(42)
list.add(42)
list.add(99)

then:
3 * list.add(42)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(99)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

3 * list.add(42)   (2 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   99       |  42
            false
    ''')
    exceptionMessage == expected
  }

  def "satisfied wildcard-count interaction is not shown"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(1)
list.add(2)

then:
_ * list.add(1)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(2)   <-- this triggered the error
    ''')
    exceptionMessage == expected
  }

  def "wildcard method interaction shown with argument mismatch"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.get(0)

then:
1 * list._(1)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.get(0)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list._(1)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   0        |  1
            false
    ''')
    exceptionMessage == expected
  }

  def "wildcard target interaction shown with argument mismatch"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)

then:
1 * _.add(1)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(2)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * _.add(1)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
    ''')
    exceptionMessage == expected
  }

  def "wildcard target and method interaction shown with argument mismatch"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)

then:
1 * _._(1)
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(2)   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * _._(1)   (0 invocations)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
    ''')
    exceptionMessage == expected
  }

  def "multiple arguments show per-argument mismatch"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(0, "a")

then:
1 * list.add(0, "b")
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * list.add(0, 'a')   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * list.add(0, "b")   (0 invocations)
One or more arguments(s) didn't match:
0: <matches>
1: argument == expected
   |        |  |
   a        |  b
            false
            1 difference (0% similarity)
            (a)
            (b)
    ''')
    exceptionMessage == expected
  }

  def "varargs interaction shows mismatch"() {
    given:
    runner.addClassImport(HasVarArgs)

    when:
    runner.runFeatureBody("""
def mock = Mock(HasVarArgs)

when:
mock.execute("a", "b")

then:
1 * mock.execute("a", "c")
0 * _
    """)

    then:
    TooManyInvocationsError e = thrown()
    def exceptionMessage = normalize(e.message)
    def expected = normalize('''
Too many invocations for:

0 * _   (1 invocation)

Matching invocations (ordered by last occurrence):

1 * mock.execute(['a', 'b'])   <-- this triggered the error

Unmatched invocations (ordered by similarity):

1 * mock.execute("a", "c")   (0 invocations)
One or more arguments(s) didn't match:
0: <matches>
1: argument == expected
   |        |  |
   b        |  c
            false
            1 difference (0% similarity)
            (b)
            (c)
    ''')
    exceptionMessage == expected
  }

  String normalize(String str) {
    EMPTY_LINE.matcher(str.normalize().trim()).replaceAll('')
  }
}

interface HasVarArgs {
  void execute(String... args)
}

class SomeStatic {
  static String compile(String str){
    str
  }
}
