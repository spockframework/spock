package org.spockframework.docs.extension

import spock.lang.Specification
import spock.lang.Subject

// tag::example-a[]
@Subject([Foo, Bar])
// end::example-a[]
// tag::example-b[]
@Subject(Foo)
@Subject(Bar)
// end::example-b[]
// tag::example-common[]
class SubjectDocSpec extends Specification {
// end::example-common[]
// tag::example-c[]
  @Subject
  Foo myFoo
// end::example-c[]
}

class Foo {
}

class Bar {
}
