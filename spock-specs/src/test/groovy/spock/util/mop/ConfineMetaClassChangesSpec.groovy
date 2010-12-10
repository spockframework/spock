/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.mop

import org.spockframework.EmbeddedSpecification

import spock.lang.*

/**
 * @author Luke Daley
 */
@Stepwise
class ConfineMetaClassChangesSpec extends EmbeddedSpecification {
  
  def setupSpec() {
    newValue = 1
  }
  
  def "change the metaclass"() {
    expect: value == 1
    when: newValue = 2
    then: value == 2
  }
  
  def "value is still changed value"() {
    expect: value == 2
  }
  
  @ConfineMetaClassChanges(String)
  def "change it again, but with restore annotation"() {
    expect: value == 2
    when: newValue = 3
    then: value == 3
  }
  
  def "changes have been reverted"() {
    expect: value == 2
  }

  @ConfineMetaClassChanges([String, String])
  def "ensure duplicate classes in restore list don't cause errors"() {
    expect: true
  }

  @ConfineMetaClassChanges([String, Integer])
  def "change more than one type"() {
    expect: 
    value == 2

    when:
    setNewValue(3, String)
    setNewValue(3, Integer)

    then:
    getValue("") == 3
    getValue(1) == 3
  }
  
  def "changes are reverted on both"() {
    expect:
    getValue("") == 2

    when:
    getValue(1)

    then:
    thrown(MissingMethodException)
  }
  
  def "exercise a spec level restore"() {
    setup:
    newValue = 2
    compiler.addImport(getClass().package)
    def specs = compiler.compileWithImports("""
      import spock.util.mop.ConfineMetaClassChanges

      $annotation
      @Stepwise
      class Spec1 extends Specification {
        def feature1() {
          expect: ConfineMetaClassChangesSpec.getValue() == 2
          when: ConfineMetaClassChangesSpec.setNewValue(3)
          then: ConfineMetaClassChangesSpec.getValue() == 3
        }
        def feature2() {
          expect: ConfineMetaClassChangesSpec.getValue() == 3
        }
      }
    """)

    when:
    def failureCount = runner.runClasses(specs).failureCount

    then:
    failureCount == 0
    value == afterValue

    where:
    annotation                  | afterValue
    ""                          | 3
    "@ConfineMetaClassChanges(String)" | 2
  }
  
  @ConfineMetaClassChanges(String)
  def "meta classes are restored after each iteration"() {
    expect:
    value == i

    when:
    newValue = i + 1

    then:
    value == i + 1
    where:
    i << [2,2]
  }
  
  def "meta class was restored after parameterised"() {
    expect:
    value == 2
  }
  
  @ConfineMetaClassChanges([])
  def "annotation with empty list/array value doesn't cause an error"(){
    expect: true
  }
  
  def cleanupSpec() {
    [String, Integer].each {
      GroovySystem.metaClassRegistry.removeMetaClass(it)
    }
  }
  
  static setNewValue(value, type = String) {
    type.metaClass.getIsolateMetaClassExtensionValue = { -> value }
  }
  
  static getValue(seed = "") {
    seed.getIsolateMetaClassExtensionValue()
  }
}
