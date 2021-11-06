package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.*

class ExceptionsInConditions extends EmbeddedSpecification {
  private static final String SCRIPT_EXECUTION_LINE_PATTERN = ~/[Ss]cript_?+\p{XDigit}++\.groovy:[\d]++/
  private static final String NORMALIZED_SCRIPT_EXECUTION_LINE = "scriptXXX.groovy:X"
  private static final String INDY_FROM_CACHE_EXECUTION_LINE_PATTERN =
    /(?m)^\s+at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache\(IndyInterface.java:\d+\)\R/

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
                       \tat apackage.ASpec.a feature($NORMALIZED_SCRIPT_EXECUTION_LINE)
        """.stripIndent()
    unifyScriptExecutionLineInStacktrace(e.condition.rendering) == expected
    e.cause instanceof NullPointerException
  }

  private String unifyScriptExecutionLineInStacktrace(String stacktrace) {
    return stacktrace.replaceAll(SCRIPT_EXECUTION_LINE_PATTERN, NORMALIZED_SCRIPT_EXECUTION_LINE)
      .replaceAll(INDY_FROM_CACHE_EXECUTION_LINE_PATTERN, "")
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
                       \tat apackage.ASpec.a feature($NORMALIZED_SCRIPT_EXECUTION_LINE)
        """.stripIndent()
    unifyScriptExecutionLineInStacktrace(e.condition.rendering) == expected
    e.cause instanceof NullPointerException
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
        \tat apackage.ASpec.a feature_closure1($NORMALIZED_SCRIPT_EXECUTION_LINE)
        \tat apackage.ASpec.a feature($NORMALIZED_SCRIPT_EXECUTION_LINE)
        """.stripIndent()
    unifyScriptExecutionLineInStacktrace(e.condition.rendering) == expected

    e.cause instanceof IllegalArgumentException
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
    e.condition.rendering == expected

    e.cause == null
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
    e.message == "key does not exists"
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
    def expected = """\
        Condition failed with Exception:

        map.get("key").b.c.d.e()
        |   |          |
        [:] null       java.lang.NullPointerException: Cannot get property 'b' on null object
                       \tat apackage.ASpec.a feature($NORMALIZED_SCRIPT_EXECUTION_LINE)
        """.stripIndent()
    unifyScriptExecutionLineInStacktrace(e.message) == expected
  }
}
