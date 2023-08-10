package org.spockframework.util

import groovy.transform.Canonical
import groovy.transform.ToString
import io.leangen.geantyref.TypeToken
import org.spockframework.runtime.model.NodeInfo
import spock.lang.Specification

import java.lang.annotation.Annotation
import java.lang.reflect.*
import java.util.function.Function

@SuppressWarnings(["unused", 'GrUnnecessaryPublicModifier'])
class GenericTypeReflectorUtilSpec extends Specification {
  private static final Type FUNC_STRING_INT_TYPE = new TypeToken<Function<String, Integer>>() {}.type
  private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>() {}.type
  private static final Type LIST_LIST_STRING_TYPE = new TypeToken<List<List<String>>>() {}.type
  private static final Type CLASS_SERIALIZABLE = new TypeToken<Class<Serializable>>() {}.type

  def "erase"(Type inputType, Class<?> expectedClass) {
    expect:
    GenericTypeReflectorUtil.erase(inputType) == expectedClass

    where:
    inputType             | expectedClass
    Object                | Object
    StringBuilder         | StringBuilder
    FUNC_STRING_INT_TYPE  | Function
    LIST_STRING_TYPE      | List
    LIST_LIST_STRING_TYPE | List
  }

  def "getTypeName"(Type inputType, String expectedName) {
    expect:
    GenericTypeReflectorUtil.getTypeName(inputType) == expectedName

    where:
    inputType             | expectedName
    Object                | "java.lang.Object"
    StringBuilder         | "java.lang.StringBuilder"
    FUNC_STRING_INT_TYPE  | "java.util.function.Function<java.lang.String, java.lang.Integer>"
    LIST_STRING_TYPE      | "java.util.List<java.lang.String>"
    LIST_LIST_STRING_TYPE | "java.util.List<java.util.List<java.lang.String>>"
  }

  def "getParameterTypes"(TestMethod testMethod, List<Type> expectedTypes) {
    expect:
    GenericTypeReflectorUtil.getParameterTypes(testMethod.method, testMethod.declaringClass).toList() == expectedTypes

    where:
    testMethod                               | expectedTypes
    method(Object, "equals")                 | [Object]
    method(List, "equals")                   | [Object]
    method(Comparator, "compare")            | [Object, Object]
    method(ListStringComparator, "compare")  | [LIST_STRING_TYPE, LIST_STRING_TYPE]
    method(DefinesCompare, "compareTo")      | [DefinesCompare]
    method(DefinesComparator, "compare")     | [DefinesCompare, DefinesCompare]
    method(NodeInfo, "getAnnotationsByType") | [Class]
    method("methodWithTwoParams")            | [String, int]
    method("genericMethod")                  | [Class]
    method("genericMethodWithTypeBound")     | [Class]
    method("genericMethodWithTwoTypeBounds") | [Class]
  }

  private abstract class TestNodeInfo extends NodeInfo<TestNodeInfo, Method> {}

  private abstract class DefinesCompare implements Comparable<DefinesCompare> {}

  private abstract class DefinesComparator implements Comparator<DefinesCompare> {}

  private abstract class ListStringComparator implements Comparator<List<String>> {}

  void methodWithTwoParams(String p1, int p2) {}

  StringBuilder exactReturnType() { null }

  public <T> T genericMethod(Class<T> arg) { null }

  public <T extends Serializable> T genericMethodWithTypeBound(Class<T> arg) { null }

  public <T extends Serializable & Runnable> T genericMethodWithTwoTypeBounds(Class<T> arg) { null }

  def "getReturnType"(TestMethod testMethod, Type expectedType) {
    expect:
    GenericTypeReflectorUtil.getReturnType(testMethod.method, testMethod.declaringClass) == expectedType

    where:
    testMethod                               | expectedType
    method(Object, "equals")                 | boolean
    method(List, "equals")                   | boolean
    method("exactReturnType")                | StringBuilder
    method("genericMethod")                  | Object
    method("genericMethodWithTypeBound")     | Serializable
    method("genericMethodWithTwoTypeBounds") | Object
    method(NodeInfo, "getAnnotation")        | Annotation
    method(NodeInfo, "getName")              | String
    method(NodeInfo, "getReflection")        | AnnotatedElement
    method(TestNodeInfo, "getReflection")    | Method
    method(NodeInfo, "getParent")            | NodeInfo
    method(TestNodeInfo, "getParent")        | TestNodeInfo
    method(ArrayList, "get")                 | Object
  }

  private static TestMethod method(Class<?> declaringClass = GenericTypeReflectorUtilSpec.class, String methodName) {
    def method = declaringClass.methods.find { it.name == methodName }
    assert declaringClass && methodName && method != null
    return new TestMethod(method, declaringClass)
  }

  @Canonical
  @ToString(includePackage = false)
  static class TestMethod {
    Method method
    Type declaringClass
  }

  def "getExactFieldType"(TestField testField, Type expectedType) {
    expect:
    GenericTypeReflectorUtil.getExactFieldType(testField.field, testField.testClass) == expectedType

    where:
    testField                                                   | expectedType
    field(TestField, "field")                                   | Field
    field(TestField, "testClass")                               | Type
    field(GenericFieldClass, "field")                           | Number
    field(SubTypeGenericFieldClass, GenericFieldClass, "field") | Double
  }

  private static TestField field(Class<?> testClass = null, Class<?> declaringClass, String fieldName) {
    def field = declaringClass.declaredFields.find { it.name == fieldName }
    assert declaringClass && fieldName && field != null
    testClass = testClass ?: declaringClass
    return new TestField(field, testClass)
  }

  @Canonical
  @ToString(includePackage = false)
  static class TestField {
    Field field
    Type testClass
  }

  static class GenericFieldClass<T extends Number> {
    T field
  }

  static class SubTypeGenericFieldClass extends GenericFieldClass<Double> {}
}
