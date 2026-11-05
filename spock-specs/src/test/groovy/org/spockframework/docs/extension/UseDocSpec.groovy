package org.spockframework.docs.extension

import groovy.transform.TypeChecked
import spock.lang.Specification
import spock.lang.SpockTypeChecked
import spock.util.mop.Use

import static groovy.transform.TypeCheckingMode.SKIP

@TypeChecked(SKIP)
// tag::example[]
class ListExtensions {
  static avg(List list) { list.sum() / list.size() }
}

// end::example[]
@SpockTypeChecked(SKIP)
// tag::example[]
class UseDocSpec extends Specification {
  @Use(ListExtensions)
  def "can use avg() method"() {
    expect:
    [1, 2, 3].avg() == 2
  }
}
// end::example[]
