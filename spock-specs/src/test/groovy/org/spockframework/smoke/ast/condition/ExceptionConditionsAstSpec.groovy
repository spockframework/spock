package org.spockframework.smoke.ast.condition

import org.spockframework.EmbeddedSpecification
import spock.lang.Issue
import spock.util.Show

class ExceptionConditionsAstSpec extends EmbeddedSpecification {

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "thrown rewrite keeps correct method reference"() {
    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def foobar = foobar()

  then:
  thrown(IllegalStateException)
}

def foobar() {
  throw new IllegalStateException("foo")
}''', EnumSet.of(Show.METHODS))
    then:
    result.source == '''\
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    java.lang.Object foobar
    this.getSpecificationContext().setThrownException(null)
    try {
        foobar = this.foobar()
    }
    catch (java.lang.Throwable $spock_ex) {
        this.getSpecificationContext().setThrownException($spock_ex)
    }
    finally {
    }
    this.thrownImpl(null, null, java.lang.IllegalStateException)
    this.getSpecificationContext().getMockController().leaveScope()
}'''

  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "thrown rewrite keeps correct method reference for multi-assignments"() {
    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def (foobar, b) = foobar()

  then:
  thrown(IllegalStateException)
}

def foobar() {
  throw new IllegalStateException("foo")
}''', EnumSet.of(Show.METHODS))
    then:
    result.source == '''\
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    def (java.lang.Object foobar, java.lang.Object b) = [null, null]
    this.getSpecificationContext().setThrownException(null)
    try {
        (foobar, b) = this.foobar()
    }
    catch (java.lang.Throwable $spock_ex) {
        this.getSpecificationContext().setThrownException($spock_ex)
    }
    finally {
    }
    this.thrownImpl(null, null, java.lang.IllegalStateException)
    this.getSpecificationContext().getMockController().leaveScope()
}'''

  }
}
