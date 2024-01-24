package org.spockframework.smoke.mock

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock(Resources.META_CLASS_REGISTRY)
@Issue('https://github.com/spockframework/spock/issues/761')
class GroovyMockGlobalClass extends Specification {

  def setup() {
    GroovySpy(ClassA, global: true)
    GroovySpy(ClassB, global: true)
  }


  def "Wildcard matching for static class methods returns correct results"() {
    given:
    def service = new Subject()
    when:
    service.myMethodUnderTest()
    then:
    (1.._) * ClassA.get(_) >> new ClassA()
    (1.._) * ClassB.get(_) >> new ClassB()
  }

}

class Subject {
  void myMethodUnderTest() {
    assert ClassA.get(1) instanceof ClassA
    assert ClassB.get(2) instanceof ClassB
  }
}

class ClassA {}

class ClassB {}
