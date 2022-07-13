package org.spockframework.docs.extension

import spock.lang.See
import spock.lang.Specification

// tag::example[]
@See("https://spockframework.org/spec")
class SeeDocSpec extends Specification {
  @See(["https://en.wikipedia.org/wiki/Levenshtein_distance", "https://www.levenshtein.net/"])
  def "Even more information is available on the feature"() {
    expect: true
  }

  @See("https://www.levenshtein.de/")
  @See(["https://en.wikipedia.org/wiki/Levenshtein_distance", "https://www.levenshtein.net/"])
  def "And even more information is available on the feature"() {
    expect: true
  }
}
// end::example[]
