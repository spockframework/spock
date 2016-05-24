package org.spockframework.smoke.condition

import junit.framework.Assert
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockException
import spock.lang.FailsWith
import spock.lang.Specification


class ExceptionsInConditions extends EmbeddedSpecification {
  def "null pointer exception in condition"() {
    when:
    runner.runFeatureBody(
        """
                def map = new HashMap<String, String>()
                expect:
                map.get("key").length() == 0
            """)

    then:
    ConditionNotSatisfiedError e = thrown()


    def expected = """\
            map.get("key").length() == 0
            |   |          |
            [:] null       java.lang.NullPointerException: Cannot invoke method length() on null object
        """.stripIndent()
    expected == e.getCondition().getRendering()
    e.getCause() instanceof NullPointerException
  }

  def "null pointer exception in method condition"() {
    when:
    runner.runFeatureBody(
        """
                def map = new HashMap<String, String>()
                expect:
                map.get("key").isEmpty()
            """)

    then:
    ConditionNotSatisfiedError e = thrown()


    def expected = """\
            map.get("key").isEmpty()
            |   |          |
            [:] null       java.lang.NullPointerException: Cannot invoke method isEmpty() on null object
        """.stripIndent()
    expected == e.getCondition().getRendering()
    e.getCause() instanceof NullPointerException
  }

  def "exception while invoking condition"() {
    when:
    runner.runFeatureBody(
        """
                    def getKey = { map, key ->
                        def result = map.get(key)
                        if (result == null) {
                            throw new IllegalArgumentException("key does not exists")
                        }
                        return result;
                    }
                  def map = new HashMap<String, String>()
                  expect:
                  getKey(map, "key").length() == 0
                """)

    then:
    ConditionNotSatisfiedError e = thrown()


    def expected = """\
            getKey(map, "key").length() == 0
            |      |
            |      [:]
            java.lang.IllegalArgumentException: key does not exists
        """.stripIndent()
    expected == e.getCondition().getRendering()

    e.getCause() instanceof IllegalArgumentException
  }


  def "spock assertion errors should be processed as is"() {
    when:
    runner.runFeatureBody(
        """
                    def getKey = { map, key ->
                        def result = map.get(key)
                        assert result
                        return result;
                    }
                  def map = new HashMap<String, String>()
                  expect:
                  getKey(map, "key").length() == 0
                """)

    then:
    ConditionNotSatisfiedError e = thrown()


    def expected = """\
            result
            |
            null
        """.stripIndent()
    expected == e.getCondition().getRendering()

    e.getCause() == null
  }

  def "spock exceptions should be processed as is"() {
    when:
    runner.runFeatureBody(
        """
                    def getKey = { map, key ->
                        def result = map.get(key)
                        if (result==null){
                            throw new org.spockframework.runtime.SpockException("key does not exists")
                        }
                        return result;
                    }
                  def map = new HashMap<String, String>()
                  expect:
                  getKey(map, "key").length() == 0
                """)

    then:
    SpockException e = thrown()
    e.getMessage() == "key does not exists"
  }

  def "deep nesting"() {
    when:
    runner.runFeatureBody(
        """
                  def map = new HashMap<String, String>()
                  expect:
                  map.get("key").b.c.d.e()
                """)

    then:
    ConditionNotSatisfiedError e = thrown()
    e.getMessage() == """\
      Condition not satisfied:

      map.get("key").b.c.d.e()
      |   |          |
      [:] null       java.lang.NullPointerException: Cannot get property 'b' on null object
      """.stripIndent()
  }
}
