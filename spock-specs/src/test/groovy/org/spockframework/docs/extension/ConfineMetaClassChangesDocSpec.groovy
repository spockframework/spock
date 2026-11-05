package org.spockframework.docs.extension

import spock.lang.Specification
import spock.lang.SpockTypeChecked
import spock.lang.Stepwise
import spock.util.mop.ConfineMetaClassChanges

import static groovy.transform.TypeCheckingMode.SKIP

@SpockTypeChecked(SKIP)
// tag::example[]
@Stepwise
class ConfineMetaClassChangesDocSpec extends Specification {
  @ConfineMetaClassChanges(String)
  def "I run first"() {
    when:
    String.metaClass.someMethod = { delegate }

    then:
    String.metaClass.hasMetaMethod('someMethod')
  }

  def "I run second"() {
    when:
    "Foo".someMethod()

    then:
    thrown(MissingMethodException)
  }
}
// end::example[]
