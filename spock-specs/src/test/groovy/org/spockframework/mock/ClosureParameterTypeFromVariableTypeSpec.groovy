package org.spockframework.mock

import org.codehaus.groovy.ast.ClassNode
import spock.lang.Specification

class ClosureParameterTypeFromVariableTypeSpec extends Specification {
  def "recognizes Specification as a MockingApi (now implemented as an interface)"() {
    expect:
    ClosureParameterTypeFromVariableType.extendsOrImplementsMockingApi(new ClassNode(Specification))
  }

  def "recognizes a subclass of Specification as a MockingApi"() {
    expect:
    ClosureParameterTypeFromVariableType.extendsOrImplementsMockingApi(new ClassNode(ClosureParameterTypeFromVariableTypeSpec))
  }

  def "does not recognize an unrelated class"() {
    expect:
    !ClosureParameterTypeFromVariableType.extendsOrImplementsMockingApi(new ClassNode(String))
  }
}
