
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

package org.spockframework.groovy

import org.spockframework.util.ReflectionUtil

import spock.lang.*

class VarArgsSpec extends Specification {
  def "Groovy supports vararg invocation style for vararg parameters"() {
    expect:
    JavaVarArgs.varArgMethod(0, "a", "b", "c").size() == 3
    GroovyVarArgs.varArgMethod(0, "a", "b", "c").size() == 3
  }

  def "Groovy supports vararg invocation style for array parameters"() {
    expect:
    JavaVarArgs.arrayMethod(0, "a", "b", "c").size() == 3
    // note: the following invocation even works from Java (albeit not (yet) with joint compilation)
    GroovyVarArgs.arrayMethod(0, "a", "b", "c").size() == 3
  }

  def "vararg reflection"() {
    expect:
    ReflectionUtil.getMethodByName(JavaVarArgs, "varArgMethod").isVarArgs()
    !ReflectionUtil.getMethodByName(JavaVarArgs, "arrayMethod").isVarArgs()
    ReflectionUtil.getMethodByName(GroovyVarArgs, "varArgMethod").isVarArgs()
    ReflectionUtil.getMethodByName(GroovyVarArgs, "arrayMethod").isVarArgs()
  }
}
