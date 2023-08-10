/*
 * Copyright 2013 the original author or authors.
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

import io.leangen.geantyref.TypeToken
import org.spockframework.mock.IMockInvocation
import spock.lang.Ignore
import spock.lang.Specification

class GenericMockInvocations extends Specification {
  interface Function<D, C> {
    C get(D value)
  }

  def "mock with parameterized type"() {
    def holder = Mock(type: new TypeToken<Function<String, Integer>>(){}.type)

    when:
    holder.get("foo")

    then:
    1 * holder.get(_) >> { IMockInvocation invocation ->
      assert invocation.method.parameterTypes == [String]
      assert invocation.method.returnType == Integer
    }
  }

  def "mock with raw type"() {
    def holder = Mock(Function)

    when:
    holder.get("foo")

    then:
    1 * holder.get(_) >> { IMockInvocation invocation ->
      assert invocation.method.parameterTypes == [Object]
      assert invocation.method.returnType == Object
      assert invocation.method.exactParameterTypes == [Object]
      assert invocation.method.exactReturnType == Object
    }
  }

  static class StringIntFunction implements Function<String, Integer> {
    Integer get(String value) {}
  }

  def "mock whose type implements parameterized interface"() {
    def holder = Mock(StringIntFunction)

    when:
    holder.get("foo")

    then:
    1 * holder.get(_) >> { IMockInvocation invocation ->
      assert invocation.method.parameterTypes == [String]
      assert invocation.method.returnType == Integer
    }
  }

  static class FunctionClass<D, C> {
    C get(D value) { null }
  }

  static class StringIntFunction2 extends FunctionClass<String, Integer> {}

  def "mock whose type extends parameterized class"() {
    def holder = Mock(StringIntFunction2)

    when:
    holder.get("foo")

    then:
    1 * holder.get(_) >> { IMockInvocation invocation ->
      assert invocation.method.parameterTypes == [String]
      assert invocation.method.returnType == Integer
    }
  }

  static class StringIntListFunction extends FunctionClass<List<String>, List<Integer>> {}

  @Ignore("appears to run into a groovy 2.3.x bug")
  def "mock with parameterized parameter and return types"() {
    def holder = Mock(StringIntListFunction)

    when:
    holder.get(["foo"])

    then:
    1 * holder.get(_) >> { IMockInvocation invocation ->
      assert invocation.method.exactParameterTypes == [new TypeToken<List<String>>(){}.type]
      assert invocation.method.exactReturnType == new TypeToken<List<Integer>>(){}.type
    }
  }

  static class GenericMethod {
    public <T> T doIt(Class<T> type) { null }
  }

  def "mock with generic method"() {
    def generic = Mock(GenericMethod)

    when:
    generic.doIt(String)

    then:
    1 * generic.doIt(_) >> { IMockInvocation invocation ->
      assert invocation.method.returnType == Object
      assert invocation.method.exactReturnType == Object
    }
  }
}
