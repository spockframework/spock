package org.spockframework.docs.extension

import spock.lang.See
import spock.lang.Specification

// tag::example[]
@See("http://spockframework.org/spec")
class SeeDocSpec extends Specification {
  @See(["http://en.wikipedia.org/wiki/Levenshtein_distance", "http://www.levenshtein.net/"])
  def "Even more information is available on the feature"() {
    expect: true
  }

  @See("http://www.levenshtein.de/")
  @See(["http://en.wikipedia.org/wiki/Levenshtein_distance", "http://www.levenshtein.net/"])
  def "And even more information is available on the feature"() {
    expect: true
  }
}
// end::example[]
