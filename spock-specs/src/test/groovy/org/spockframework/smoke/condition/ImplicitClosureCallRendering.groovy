/*
* Copyright 2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*     https://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.spockframework.smoke.condition

/**
 * Describes rendering of conditions that contain a closure call
 * with the implicit "foo(args)" syntax instead of the explicit
 * "foo.call(args)" syntax.
 *
 * @author Peter Niederwieser
 */
class ImplicitClosureCallRendering extends ConditionRenderingSpec {
  def "with local variable"() {
    expect:
    isRendered """
func(42) == null
|        |
42       false
        """, {
      def func = { it }
      assert func(42) == null
    }
  }

  def assertFunc(func) {
    assert func(42) == null
  }

  def "with method argument"() {
    expect:
    isRendered """
func(42) == null
|        |
42       false
        """, {
      assertFunc { it }
    }
  }

  private funcField = { it }

  def "with field"() {
    expect:
    isRendered """
funcField(42) == null
|             |
42            false
        """, {
      assert funcField(42) == null
    }
  }

  def func = { it }

  def "with property"() {
    expect:
    isRendered """
func(42) == null
|        |
42       false
        """, {
      assert func(42) == null
    }
  }

  def "with qualified property"() {
    def holder = new FuncHolder()

    expect:
    isRendered """
holder.func(42) == null
|      |        |
|      42       false
${holder.dump()}
        """, {
      assert holder.func(42) == null
    }
  }

  // for implicit closure calls that don't
  // look like method calls, we don't currently
  // render the return value (little practical value,
  // complicates implementation, unclear how to
  // render in an intuitive way)

  def "with method call"() {
    expect:
    isRendered """
getFunc()(42) == null
|             |
|             false
${getFunc().dump()}
        """, {
      assert getFunc()(42) == null
    }
  }

  def "with qualified method call"() {
    def holder = new FuncHolder()

    expect:
    isRendered """
holder.getFunc()(42) == null
|      |             |
|      |             false
|      ${holder.func.dump()}
${holder.dump()}
        """, {
      assert holder.getFunc()(42) == null
    }
  }

  static class FuncHolder {
    def func = { it }
  }

}

