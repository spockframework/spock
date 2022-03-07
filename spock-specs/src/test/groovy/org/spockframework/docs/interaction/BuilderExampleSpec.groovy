package org.spockframework.docs.interaction

import spock.lang.Specification

import groovy.transform.builder.*

class BuilderExampleSpec extends Specification {

  def "build example"() {
    // tag::builder[]
    given:
    ThingBuilder builder = Mock() {
      _ >> _
    }

    when:
    Thing thing = builder
      .id("id-42")
      .name("spock")
      .weight(100)
      .build()

    then:
    1 * builder.build() >> new Thing(id: 'id-1337')
    thing.id == 'id-1337'
    // end::builder[]
  }
}


class Thing {
  String name
  String id
  int weight
}


@Builder(builderStrategy = ExternalStrategy, forClass = Thing)
class ThingBuilder {}
