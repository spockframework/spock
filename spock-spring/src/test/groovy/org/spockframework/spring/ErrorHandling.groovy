package org.spockframework.spring

import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import org.springframework.test.context.*

class ErrorHandling extends Specification {

  def "Exceptions in afterTest are reported normally"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.*
import spock.lang.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(ThrowAfterTest)
@ContextConfiguration(classes = Object)
class Foo extends Specification {

  def foo() {
    expect:
    1==1
  }
}
""")

    then:
    result.failureCount == 1
    result.failures[0].message == 'afterTestMethod'
  }

  def "Exceptions in afterTest are reported as suppressed exceptions if cleanup also throws"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.*
import spock.lang.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(ThrowAfterTest)
@ContextConfiguration(classes = Object)
class Foo extends Specification {

  def foo() {
    expect:
    1==1
    cleanup:
    throw new RuntimeException("cleanup")
  }
}
""")

    then:
    result.failureCount == 1
    result.failures[0].message == 'cleanup'
    result.failures[0].exception.suppressed[0].message == 'afterTestMethod'
  }

  def "Exceptions in afterClass are reported normally"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.*
import spock.lang.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(ThrowAfterClass)
@ContextConfiguration(classes = Object)
class Foo extends Specification {

  def foo() {
    expect:
    1==1
  }
}
""")

    then:
    result.failureCount == 1
    result.failures[0].message == 'afterTestClass'
  }

  def "Exceptions in afterClass are reported as suppressed exceptions if cleanup also throws"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.*
import spock.lang.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(ThrowAfterClass)
@ContextConfiguration(classes = Object)
class Foo extends Specification {

  def foo() {
    expect:
    1==1
  }
  
  def cleanupSpec() {
    throw new RuntimeException("cleanupSpec")
  }
}
""")

    then:
    result.failureCount == 1
    result.failures[0].message == 'cleanupSpec'
    result.failures[0].exception.suppressed[0].message == 'afterTestClass'
  }
}

class ThrowAfterTest implements TestExecutionListener {

  @Override
  void beforeTestClass(TestContext testContext) throws Exception {}

  @Override
  void prepareTestInstance(TestContext testContext) throws Exception {}

  @Override
  void beforeTestMethod(TestContext testContext) throws Exception {}

  @Override
  void afterTestMethod(TestContext testContext) throws Exception {
    throw new RuntimeException("afterTestMethod")
  }

  @Override
  void afterTestClass(TestContext testContext) throws Exception {}
}

class ThrowAfterClass implements TestExecutionListener {

  @Override
  void beforeTestClass(TestContext testContext) throws Exception {}

  @Override
  void prepareTestInstance(TestContext testContext) throws Exception {}

  @Override
  void beforeTestMethod(TestContext testContext) throws Exception {}

  @Override
  void afterTestMethod(TestContext testContext) throws Exception {}

  @Override
  void afterTestClass(TestContext testContext) throws Exception {
    throw new RuntimeException("afterTestClass")
  }
}
