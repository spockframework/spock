/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime

import spock.lang.*

import static GroovyRuntimeUtil.isVoidMethod

class GroovyRuntimeUtilIsVoidMethodSpec extends Specification {
  def "void Java instance method"() {
    expect:
    isVoidMethod(new JavaMethods(), "voidInstanceMethod")
  }

  def "non-void Java instance method"() {
    expect:
    !isVoidMethod(new JavaMethods(), "nonVoidInstanceMethod")
  }

  def "void Java static method"() {
    expect:
    isVoidMethod(new JavaMethods(), "voidStaticMethod")
    isVoidMethod(JavaMethods, "voidStaticMethod")
  }

  def "non-void Java static method"() {
    expect:
    !isVoidMethod(new JavaMethods(), "nonVoidStaticMethod")
    !isVoidMethod(JavaMethods, "nonVoidStaticMethod")
  }

  def "void Groovy instance method"() {
    expect:
    isVoidMethod(new GroovyMethods(), "voidInstanceMethod")
  }

  def "untyped Groovy instance method"() {
    expect:
    !isVoidMethod(new GroovyMethods(), "untypedInstanceMethodWithRetVal")
    !isVoidMethod(new GroovyMethods(), "untypedInstanceMethodWithoutRetVal")
  }

  def "typed Groovy instance method"() {
    expect:
    !isVoidMethod(new GroovyMethods(), "typedInstanceMethodWithRetVal")
    !isVoidMethod(new GroovyMethods(), "typedInstanceMethodWithoutRetVal")
  }

  def "void Groovy static method"() {
    expect:
    isVoidMethod(new GroovyMethods(), "voidStaticMethod")
    isVoidMethod(GroovyMethods, "voidStaticMethod")
  }

  def "untyped Groovy static method"() {
    expect:
    !isVoidMethod(new GroovyMethods(), "untypedStaticMethodWithRetVal")
    !isVoidMethod(GroovyMethods, "untypedStaticMethodWithRetVal")
    !isVoidMethod(new GroovyMethods(), "untypedStaticMethodWithoutRetVal")
    !isVoidMethod(GroovyMethods, "untypedStaticMethodWithoutRetVal")
  }

  def "typed Groovy static method"() {
    expect:
    !isVoidMethod(new GroovyMethods(), "typedStaticMethodWithRetVal")
    !isVoidMethod(GroovyMethods, "typedStaticMethodWithRetVal")
    !isVoidMethod(new GroovyMethods(), "typedStaticMethodWithoutRetVal")
    !isVoidMethod(GroovyMethods, "typedStaticMethodWithoutRetVal")
  }

  def "non-void java.lang.Class method"() {
    expect:
    !isVoidMethod(int, "getSuperclass")
  }

  def "void java.lang.Class method"() {
    expect:
    synchronized(Collections) {
      assert isVoidMethod(Collections, "notify")
    }
  }

  def "void DGM method"() {
    expect:
    isVoidMethod(this, "print", [""] as Object[])
    isVoidMethod(3, "times", [{}] as Object[])
  }

  def "non-void DGM method"() {
    expect:
    !isVoidMethod("a", "find", ["b"] as Object[])
  }

  def "void DGSM method"() {
    expect:
    isVoidMethod(this, "sleep", [1] as Object[])
    isVoidMethod(Object, "sleep", [1] as Object[])
  }

  def "non-void DGSM method"() {
    expect:
    !isVoidMethod(Thread, "start", [ {} ] as Object[])
  }

  def "Groovy method that takes a String with a GString"() {
    expect:
    !isVoidMethod(new GroovyMethods(),
        "methodWithStringParam", ["one ${"t" + "w" + "o"} three"] as Object[])
  }

  def "method with null target"() {
    expect:
    !isVoidMethod(null, "foo", 1)
  }
}
