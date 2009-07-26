package org.spockframework.example

import org.junit.runner.RunWith

import spock.lang.*

/**
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class FeatureUnrollingExample {
  def "without unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }

  @Unroll
  def "with unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }

  @Unroll("length of '#name' should be #length")
  def "with unrolling and custom display names"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }
}