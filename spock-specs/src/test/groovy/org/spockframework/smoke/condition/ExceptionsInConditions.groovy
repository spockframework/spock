package org.spockframework.smoke.condition

import junit.framework.Assert
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockException
import spock.lang.FailsWith
import spock.lang.Specification


class ExceptionsInConditions extends ConditionRenderingSpec {
  def "null pointer exception in condition"() {
    expect:
    isRendered("""\
            map.get("key").length() == 0
            |   |          |
            [:] null       java.lang.NullPointerException: Cannot invoke method length() on null object
        """.stripIndent(),
        {
          def map = new HashMap<String, String>()
          assert map.get("key").length() == 0
        }
    )
  }


  def "exception while invoking methods condition"() {
    expect:
    isRendered("""\
            getKey(map, "key").length() == 0
            |      |
            |      [:]
            java.lang.IllegalArgumentException: key does not exists
        """.stripIndent(),
        {
          def getKey = { map, key ->
            def result = map.get(key)
            if (result == null) {
              throw new IllegalArgumentException("key does not exists")
            }
            return result;
          }
          def map = new HashMap<String, String>()
          assert getKey(map, "key").length() == 0
        }
    )
  }

  def "spock assertion errors should be processed as is"() {
    expect:
    isRendered("""\
            result != null
            |      |
            null   false
        """.stripIndent(),
        {
          def getKey = { map, key ->
            def result = map.get(key)
            assert result != null
            return result;
          }
          def map = new HashMap<String, String>()
          assert getKey(map, "key").length() == 0
        }
    )
  }

  @FailsWith(SpockException)
  def "spock exceptions should be processed as is"() {
    def getKey = { map, key ->
      def result = map.get(key)
      if (result == null) {
        throw new SpockException("key does not exists")
      }
      return result;
    }
    def map = new HashMap<String, String>()

    expect:
    getKey(map, "key").length() == 0

  }
}
