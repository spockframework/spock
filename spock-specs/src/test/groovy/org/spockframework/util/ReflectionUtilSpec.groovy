/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.util

import org.codehaus.groovy.ast.stmt.Statement
import org.junit.platform.commons.annotation.Testable
import org.spockframework.mock.runtime.ByteBuddyTestClassLoader
import spock.lang.*

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.util.jar.JarFile

class ReflectionUtilSpec extends Specification {
  def "get package name"() {
    expect:
    ReflectionUtil.getPackageName(ReflectionUtilSpec) == "org.spockframework.util"
  }

  def "load existing class"() {
    expect:
    ReflectionUtil.loadClassIfAvailable("java.util.List") == List
  }

  def "load unexisting class"() {
    expect:
    ReflectionUtil.loadClassIfAvailable("not.AvailableClass") == null
  }

  def "load class from ContextClassloader"() {
    given:
    def oldLoader = Thread.currentThread().getContextClassLoader()

    def className = "test.TestIf"
    def cl = new ByteBuddyTestClassLoader()
    cl.defineInterface(className)

    expect:
    ReflectionUtil.loadClassIfAvailable(className) == null

    when:
    Thread.currentThread().setContextClassLoader(cl)
    then:
    ReflectionUtil.loadClassIfAvailable(className).classLoader == cl

    cleanup:
    Thread.currentThread().setContextClassLoader(oldLoader)
  }

  def getResourcesFromClassLoader() {
    when:
    def res = ReflectionUtil.getResourcesFromClassLoader(JarFile.MANIFEST_NAME)
    then:
    res.size() >= 1
  }

  @Issue("https://github.com/spockframework/spock/issues/2076")
  def "getResourcesFromClassLoaders - ContextClassLoader"() {
    given:
    def oldLoader = Thread.currentThread().getContextClassLoader()

    def resPath = "TestResourceFileName"
    def resCl = new ClassLoader() {
      @Override
      Enumeration<URL> getResources(String name) throws IOException {
        if (name == resPath) {
          return new Vector([new URL("file:/" + resPath)]).elements()
        }
        return super.getResources(name)
      }
    }

    expect:
    ReflectionUtil.getResourcesFromClassLoader(resPath).isEmpty()

    when:
    Thread.currentThread().setContextClassLoader(resCl)
    def res = ReflectionUtil.getResourcesFromClassLoader(resPath)
    then:
    res.size() == 1
    res[0].toString().contains(resPath)

    cleanup:
    Thread.currentThread().setContextClassLoader(oldLoader)
  }

  @Issue("https://github.com/spockframework/spock/issues/2076")
  def "getResourcesFromClassLoaders - ContextClassLoader == null"() {
    given:
    def oldLoader = Thread.currentThread().getContextClassLoader()

    when:
    Thread.currentThread().setContextClassLoader(null)
    def res = ReflectionUtil.getResourcesFromClassLoader(JarFile.MANIFEST_NAME)
    then:
    res.size() >= 1

    cleanup:
    Thread.currentThread().setContextClassLoader(oldLoader)
  }

  def "check if class exists"() {
    expect:
    ReflectionUtil.isClassAvailable("java.util.List")
    !ReflectionUtil.isClassAvailable("not.AvailableClass")
  }

  def "check if method exists"() {
    expect:
    ReflectionUtil.isMethodAvailable("java.util.List", "size")
    !ReflectionUtil.isMethodAvailable("java.util.List", "mice")
    !ReflectionUtil.isMethodAvailable("not.AvailableClass", "size")
  }

  def "check if annotation of certain type is present"() {
    expect:
    ReflectionUtil.isAnnotationPresent(Stepwise, "org.spockframework.runtime.extension.ExtensionAnnotation")
    !ReflectionUtil.isAnnotationPresent(Stepwise, "foo.bar.Baz")
  }

  static final class FinalClass {
    void foo() {}
  }

  static class FinalMethod {
    final void foo() {}
  }

  def "check if method is final"() {
    expect:
    ReflectionUtil.isFinalMethod(FinalClass.getDeclaredMethod("foo"))
    ReflectionUtil.isFinalMethod(FinalMethod.getDeclaredMethod("foo"))
  }

  def "find method by name"() {
    expect:
    ReflectionUtil.getMethodByName(Derived, "baseMethod") == Base.getDeclaredMethod("baseMethod", String, int)
  }

  def "find declared method by name"() {
    expect:
    ReflectionUtil.getDeclaredMethodByName(Derived, "derivedMethod") == Derived.getDeclaredMethod("derivedMethod", String)
  }

  def "find method by name and parameter types"() {
    expect:
    ReflectionUtil.getMethodBySignature(Derived, "overloadedMethod", int) == Derived.getDeclaredMethod("overloadedMethod", int)
    ReflectionUtil.getMethodBySignature(Derived, "overloadedMethod", List) == null
  }

  def "find declared method by name and parameter types"() {
    expect:
    ReflectionUtil.getDeclaredMethodBySignature(Derived, "derivedMethod", String) == Derived.getDeclaredMethod("derivedMethod", String)
    ReflectionUtil.getDeclaredMethodBySignature(Derived, "derivedMethod", List) == null
  }

  def "find class file for a class"() {
    expect:
    def file = ReflectionUtil.getClassFile(ReflectionUtilSpec)
    file != null
    file.exists()
    file.name == "ReflectionUtilSpec.class"
  }

  def "get default value for a type"() {
    expect:
    ReflectionUtil.getDefaultValue(type) == defaultValue

    where:
    type    | defaultValue
    boolean | false
    byte    | 0
    short   | 0
    char    | 0
    int     | 0
    long    | 0
    float   | 0
    double  | 0
    Object  | null
    void    | null
    Void    | null
  }

  def "check if a value has one of several types"() {
    expect:
    ReflectionUtil.hasAnyOfTypes([1], Map, ArrayList, Set)
    ReflectionUtil.hasAnyOfTypes([1], Map, Collection, Set)
    !ReflectionUtil.hasAnyOfTypes([1], Map, Set)
    !ReflectionUtil.hasAnyOfTypes([1])
  }

  def "get types of several objects"() {
    expect:
    ReflectionUtil.getTypes([1], [a: 1]) == [ArrayList, LinkedHashMap] as Class[]
    ReflectionUtil.getTypes() == [] as Class[]
  }

  def "invoke good method"() {
    def method = InvokeMe.getDeclaredMethod("good", int)

    expect:
    ReflectionUtil.invokeMethod(new InvokeMe(), method, 42) == 42
  }

  def "invoke method that throws unchecked exception"() {
    def method = InvokeMe.getDeclaredMethod("unchecked")

    when:
    ReflectionUtil.invokeMethod(new InvokeMe(), method)

    then:
    thrown(IllegalArgumentException)
  }

  def "invoke method that throws checked exception"() {
    def method = InvokeMe.getDeclaredMethod("checked")

    when:
    ReflectionUtil.invokeMethod(new InvokeMe(), method)

    then:
    thrown(IOException)
  }

  def "erase types"() {
    def method = Generics.methods.find { it.name == "foo" }
    def types = method.getGenericParameterTypes() as List

    expect:
    ReflectionUtil.eraseTypes(types) == [Object, List, List]
  }


  def "detects overridden toString"(Class clazz) {
    expect:
    ReflectionUtil.isToStringOverridden(clazz)

    where:
    clazz << [String, ArrayList, AbstractCollection]
  }

  def "detects not overridden toString"(Class clazz) {
    expect:
    !ReflectionUtil.isToStringOverridden(clazz)

    where:
    clazz << [Object, Base, InvokeMe, Generics]
  }

  static class Base {
    def baseMethod(String arg1, int arg2) {}
  }

  static class Derived extends Base {
    private derivedMethod(String arg) {}

    void overloadedMethod(String arg) {}

    void overloadedMethod(int arg) {}
  }

  static class InvokeMe {
    def good(int arg) { arg }
    def unchecked() { throw new IllegalArgumentException("oops") }
    def checked() { throw new IOException("ouch") }
  }

  static class Generics<T> {
    void foo(T one, List<T> two, List<String> three) {}
  }

  def "isAnnotationPresentRecursive"(Class<?> clazz, Class<? extends Annotation> annotation, boolean expectedResult) {
    expect:
    ReflectionUtil.isAnnotationPresentRecursive(clazz, annotation) == expectedResult
    where:
    clazz              | annotation | expectedResult
    Object             | Nullable   | false
    Specification      | Testable   | true
    ReflectionUtilSpec | Testable   | true
    ReflectionUtilSpec | Nullable   | false
    TypeWithAnno       | Narrative  | true
    SubTypeNoAnno      | Narrative  | true
  }

  def "getAnnotationRecursive expecting annotation"(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    when:
    def anno = ReflectionUtil.getAnnotationRecursive(clazz, annotationClass)
    then:
    clazz && annotationClass && anno != null
    where:
    clazz              | annotationClass
    Specification      | Testable
    ReflectionUtilSpec | Testable
    TypeWithAnno       | Narrative
    SubTypeNoAnno      | Narrative
  }

  def "getAnnotationRecursive expecting none"(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    when:
    def anno = ReflectionUtil.getAnnotationRecursive(clazz, annotationClass)
    then:
    clazz && annotationClass && anno == null
    where:
    clazz              | annotationClass
    Object             | Nullable
    ReflectionUtilSpec | Nullable
  }

  def "collectAnnotationRecursive"(Class<?> clazz, Class<? extends Annotation> annotationClass, int expectedCount) {
    expect:
    ReflectionUtil.collectAnnotationRecursive(clazz, annotationClass).size() == expectedCount
    where:
    clazz              | annotationClass | expectedCount
    Object             | Nullable        | 0
    Specification      | Testable        | 1
    ReflectionUtilSpec | Testable        | 2
    ReflectionUtilSpec | Nullable        | 0
    TypeWithAnno       | Narrative       | 1
    SubTypeNoAnno      | Narrative       | 1
    SubTypeWithAnno    | Narrative       | 2
  }

  @Narrative
  private static class TypeWithAnno {}

  @Narrative
  private static class SubTypeWithAnno extends TypeWithAnno {}

  private static class SubTypeNoAnno extends TypeWithAnno {}

  def "validateArguments - wrong argument count"() {
    when:
    ReflectionUtil.validateArguments(lookupMethod("methodNoArgs"), "Str")
    then:
    IllegalArgumentException ex = thrown()
    ex.message == "Method 'methodNoArgs([])' can't be called with parameters '[Str]'!"
  }

  def "validateArguments - incompatible type"() {
    when: "Incompatible type"
    ReflectionUtil.validateArguments(lookupMethod("methodOneArg"), 1)
    then:
    IllegalArgumentException ex = thrown()
    ex.message == "Method 'methodOneArg([class java.lang.String])' can't be called with parameters '[1]'!"
  }

  def "validateArguments - null value on primitive type"() {
    when: "Null value on primitive type"
    ReflectionUtil.validateArguments(lookupMethod("methodPrimitiveArg"), [null] as Object[])
    then:
    thrown(IllegalArgumentException)
  }

  def "validateArguments - Correct types"() {
    when: "Correct types"
    ReflectionUtil.validateArguments(lookupMethod("methodOneArg"), "Str")
    then:
    noExceptionThrown()
  }

  private Method lookupMethod(String name) {
    return Objects.requireNonNull(ReflectionUtilSpec.class.getMethods().find { it.name == name })
  }

  @SuppressWarnings('unused')
  static void methodNoArgs() {}

  @SuppressWarnings('unused')
  static void methodOneArg(String s) {}

  @SuppressWarnings('unused')
  static void methodPrimitiveArg(int value) {}

  def "deepCopyFields errors"() {
    when:
    ReflectionUtil.deepCopyFields("Str", 10)
    then:
    IllegalArgumentException ex = thrown()
    ex.message == "source and target are not compatible."
  }

  def "getClassFile"() {
    expect:
    ReflectionUtil.getClassFile(String) == null
    ReflectionUtil.getClassFile(Specification) == null
    ReflectionUtil.getClassFile(ReflectionUtilSpec) != null
  }

  def "isToStringOverridden error"() {
    expect:
    !ReflectionUtil.isToStringOverridden(int.class)
  }

  def "isClassVisibleInClassloader"(Class<?> cls, ClassLoader loader, boolean expectedResult) {
    expect:
    ReflectionUtil.isClassVisibleInClassloader(cls, loader) == expectedResult

    where:
    cls        | loader                                                  | expectedResult
    Runnable   | this.class.classLoader                                  | true
    this.class | this.class.classLoader                                  | true
    this.class | new URLClassLoader([] as URL[], this.class.classLoader) | true

    this.class | new URLClassLoader([] as URL[], null as ClassLoader)    | false
    this.class | ByteBuddyTestClassLoader.withInterface(this.class.name) | false
  }

  def "setFieldValue works"() {
    given:
    def statement = new Statement()

    expect:
    statement.statementLabel == null

    when:
    statement.addStatementLabel("label")

    then:
    statement.statementLabel == "label"

    when:
    ReflectionUtil.setFieldValue(statement, "statementLabels", null)

    then:
    statement.statementLabel == null
  }

  def "setFieldValue fails"() {
    when:
    ReflectionUtil.setFieldValue(new Object(), "nonExistent", "label")

    then:
    NoSuchFieldException ex = thrown()
  }

}
