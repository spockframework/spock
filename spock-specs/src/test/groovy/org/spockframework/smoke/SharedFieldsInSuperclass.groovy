package org.spockframework.smoke

import spock.lang.*

abstract class SharedFieldsInSuperclassBase extends Specification {
  @Shared private String sharedPrivate = "sharedPrivate"
  @Shared protected String sharedProtected = "sharedProtected"
  @Shared public String sharedPublic = "sharedPublic"
  @Shared String sharedProperty = "sharedProperty"

  @Issue("https://github.com/spockframework/spock/issues/273")
  def "can access shared private field"() {
    expect: sharedPrivate == "sharedPrivate"
  }

  def "can access shared protected field"() {
    expect: sharedProtected == "sharedProtected"
  }

  def "can access shared public field"() {
    expect: sharedPublic == "sharedPublic"
  }

  def "can access shared property"() {
    expect: sharedProperty == "sharedProperty"
  }
}

class SharedFieldsInSuperclass extends SharedFieldsInSuperclassBase {}
