package org.spockframework.smoke.mock

import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.Unroll

class GroovyMocks extends Specification {
  def "implement GroovyObject"() {
    expect:
    GroovyMock(List) instanceof GroovyObject
    GroovyMock(ArrayList) instanceof GroovyObject
  }

  @ResourceLock(Resources.META_CLASS_REGISTRY)
  @Unroll("A Groovy#typeB can't be created for a type that was already Groovy#typeA'd")
  def "global GroovyMocks can't be created for a type that is already mocked"(String typeA, String typeB) {
    given:
    createMock(typeA)

    when:
    createMock(typeB)

    then:
    CannotCreateMockException e = thrown()
    e.message == 'Cannot create mock for class java.util.ArrayList. The given type is already mocked by Spock.'

    where:
    [typeA, typeB] << ([['Mock', 'Stub', 'Spy']] * 2).combinations()
  }

  void createMock(String type) {
    switch (type) {
      case 'Mock':
        GroovyMock(global: true, ArrayList)
        break
      case 'Stub':
        GroovyStub(global: true, ArrayList)
        break
      case 'Spy':
        GroovySpy(global: true, ArrayList)
        break
      default:
        throw new IllegalArgumentException(type)
    }
  }
}
