package org.spockframework.docs.extension

import spock.lang.Specification
import spock.util.mop.Use

// tag::example[]
class ListExtensions {
  static avg(List list) { list.sum() / list.size() }
}

class UseDocSpec extends Specification {
  @Use(ListExtensions)
  def "can use avg() method"() {
    expect:
    [1, 2, 3].avg() == 2
  }
}
// end::example[]
