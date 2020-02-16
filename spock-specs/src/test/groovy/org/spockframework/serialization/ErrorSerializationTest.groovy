package org.spockframework.serialization

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.*
import org.spockframework.runtime.*
import spock.lang.Issue

@Issue('https://github.com/spockframework/spock/issues/709')
class ErrorSerializationTest extends EmbeddedSpecification {

  def setup() {
    runner.throwFailure = false
  }

  def "SpockComparisonFailure"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        expect:
        1 == 2
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof SpockComparisonFailure

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }


  def "ConditionNotSatisfiedError"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        expect:
        false
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof ConditionNotSatisfiedError

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }

  def "ConditionFailedWithExceptionError"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        expect:
        [].get(1).toString()
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof ConditionFailedWithExceptionError

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }

  def "TooFewInvocationsError"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        given:
        def list = Mock(List)
        
        when:
        list.add(1)
        
        then:
        2 * list.add(_)
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof TooFewInvocationsError

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }

  def "TooManyInvocationsError"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        given:
        def list = Mock(List)
        
        when:
        list.add(1)
        
        then:
        0 * list.add(_)
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof TooManyInvocationsError

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }

  def "WrongInvocationOrderError"() {
    when:
    def result = runner.runSpecBody """
      def fail() {
        given:
        def list = Mock(List)
        
        when:
        list.add(1)
        list.add(2)
    
        then:
        1 * list.add(2)
    
        then:
        1 * list.add(1)
      }
    """

    then:
    result.testsFailedCount == 1
    def srcThrowable = result.failures[0].exception
    srcThrowable instanceof WrongInvocationOrderError

    when:
    def deserialized = serializeAndDeserialize(srcThrowable)

    then:
    srcThrowable.message == deserialized.message
  }

  Throwable serializeAndDeserialize(Throwable e) {
    def outputStream = new ByteArrayOutputStream()
    new ObjectOutputStream(outputStream).withCloseable { it.writeObject(e) }
    return (Throwable)new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray())).readObject()
  }
}
