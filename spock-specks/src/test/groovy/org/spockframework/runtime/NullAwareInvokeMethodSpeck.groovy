/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime

import spock.lang.*
import static spock.lang.Predef.*
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
class NullAwareInvokeMethodSpeck {
  def "invoke void Java instance method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new JavaMethods(), "voidInstanceMethod", null) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke non-void Java instance method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new JavaMethods(), "nonVoidInstanceMethod", null) == null
  }

  def "invoke void Java static method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new JavaMethods(), "voidStaticMethod", null) == SpockRuntime.VOID_RETURN_VALUE
    SpockRuntime.nullAwareInvokeMethod(JavaMethods, "voidStaticMethod", null) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke non-void Java static method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new JavaMethods(), "nonVoidStaticMethod", null) == null
    SpockRuntime.nullAwareInvokeMethod(JavaMethods, "nonVoidStaticMethod", null) == null
  }

  def "invoke void Groovy instance method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "voidInstanceMethod", null) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke untyped Groovy instance method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "untypedInstanceMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "untypedInstanceMethodWithoutRetVal", null) == null
  }

  def "invoke typed Groovy instance method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "typedInstanceMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "typedInstanceMethodWithoutRetVal", null) == null
  }

  def "invoke void Groovy static method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "voidStaticMethod", null) == SpockRuntime.VOID_RETURN_VALUE
    SpockRuntime.nullAwareInvokeMethod(GroovyMethods, "voidStaticMethod", null) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke untyped Groovy static method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "untypedStaticMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(GroovyMethods, "untypedStaticMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "untypedStaticMethodWithoutRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(GroovyMethods, "untypedStaticMethodWithoutRetVal", null) == null
  }

  def "invoke typed Groovy static method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "typedStaticMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(GroovyMethods, "typedStaticMethodWithRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(), "typedStaticMethodWithoutRetVal", null) == null
    SpockRuntime.nullAwareInvokeMethod(GroovyMethods, "typedStaticMethodWithoutRetVal", null) == null
  }

  def "invoke non-void java.lang.Class method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(int, "getSuperclass", null) == null
  }

  def "invoke void java.lang.Class method"() {
    expect:
    synchronized(Collections) {
      assert SpockRuntime.nullAwareInvokeMethod(Collections, "notify", null) == SpockRuntime.VOID_RETURN_VALUE
    }
  }

  def "invoke void DGM method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(this, "println", ["abc"] as Object[]) == SpockRuntime.VOID_RETURN_VALUE
    SpockRuntime.nullAwareInvokeMethod(3, "times", [{}] as Object[]) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke non-void DGM method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod("a", "find", ["b"] as Object[]) == null
  }

  def "invoke void DGSM method"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(this, "sleep", [1] as Object[]) == SpockRuntime.VOID_RETURN_VALUE
    SpockRuntime.nullAwareInvokeMethod(Object, "sleep", [1] as Object[]) == SpockRuntime.VOID_RETURN_VALUE
  }

  def "invoke non-void DGSM method"() {
    // none of the DGSM methods reproducibly returns null, so we invoke another one
    expect:
    SpockRuntime.nullAwareInvokeMethod(Thread, "start", [ {} ] as Object[]) instanceof Thread
  }

  def "invoke Groovy method that takes a String with a GString"() {
    expect:
    SpockRuntime.nullAwareInvokeMethod(new GroovyMethods(),
        "methodWithStringParam", ["one ${"t" + "w" + "o"} three"] as Object[]) == null
  }

  def "invoke null-safe"() {
    expect:
    SpockRuntime.nullAwareInvokeMethodSafe(null, "foo", null) == null
  }

  def "invoke not null-safe"() {
    when:
    SpockRuntime.nullAwareInvokeMethod(null, "foo", null) == null

    then:
    thrown(NullPointerException)
  }
}