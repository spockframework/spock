package org.spockframework.util

import spock.lang.Specification

class ClassUtilSpec extends Specification {
  def 'get string representation of non-null class'() {
    expect:
    ClassUtil.nullSafeToString(clazz) == clazz.toString()
    where:
    clazz << [Set.class, Tuple.class, ClassUtil.class]
  }

  def 'get string representation of null class'() {
    when:
    String result = ClassUtil.nullSafeToString(null)
    then:
    notThrown(NullPointerException)
    result == 'null'
  }

  def 'get string representation of multiple potentially null classes'(Class<?>... clazzes) {
    given:
    def clazzStrings = []
    for (clazz in clazzes) {
      clazzStrings.add(ClassUtil.nullSafeToString(clazz))
    }
    when:
    String result = ClassUtil.allNullSafeToString(clazzes)
    then: 'result is names from single class overload separated by a comma and space each'
    notThrown(NullPointerException)
    result == String.join(', ', clazzStrings)
    where:
    clazzes << [[List.class, Set.class, Queue.class, Map.class],
                [File.class, null, URL.class],
                [null, null]]
  }

  def 'get string representation of one class not known to be just one'() {
    given:
    Class<?>[] singleton = [Specification.class]
    Class<?>[] singletonOfNull = [null]
    expect:
    ClassUtil.allNullSafeToString(singleton) == ClassUtil.nullSafeToString(Specification.class)
    ClassUtil.allNullSafeToString(singletonOfNull) == ClassUtil.nullSafeToString(null)
  }

  def 'get string representation of no classes'() {
    expect:
    ClassUtil.allNullSafeToString() == ''
  }

  def 'get string representation of null array of classes'() {
    given:
    Class<?>[] nullArray = null
    when:
    String result = ClassUtil.allNullSafeToString(nullArray)
    then:
    notThrown(NullPointerException)
    result == ''
  }
}
