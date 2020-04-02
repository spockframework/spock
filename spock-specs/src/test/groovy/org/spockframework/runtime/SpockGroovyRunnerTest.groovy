package org.spockframework.runtime

import spock.lang.Specification

class SpockGroovyRunnerTest extends Specification {
  def 'running successful specification does not throw exception'() {
    when:
    new GroovyShell().run '''
import spock.lang.Specification

class Foo extends Specification {
  def foo() {
    expect:
    true
  }
}
''', 'foo.groovy'

    then:
    noExceptionThrown()
  }

  def 'running failing specification throws exception'() {
    when:
    new GroovyShell().run '''
import spock.lang.Specification

class Foo extends Specification {
  def foo() {
    expect:
    false
  }
}
''', 'foo.groovy'

    then:
    thrown(ConditionNotSatisfiedError)
  }
}
