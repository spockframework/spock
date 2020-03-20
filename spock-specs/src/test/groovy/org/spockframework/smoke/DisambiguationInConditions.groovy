package org.spockframework.smoke

import spock.lang.Specification

import java.util.regex.Pattern

import static DisambiguationInConditionsTestee.Java1
import static DisambiguationInConditionsTestee.Java2
import static DisambiguationInConditionsTestee.Java3
import static DisambiguationInConditionsTestee.Java4
import static DisambiguationInConditionsTestee.Verifier

class DisambiguationInConditions extends Specification {
  Verifier verifier = Mock()

  def 'test java class forwarding to java class without type cast'() {
    when:
    new Java2(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  def 'test java class forwarding to java class with type cast'() {
    when:
    new Java3(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  def 'test java class not forwarding'() {
    when:
    new Java4(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  def 'test groovy class forwarding to java class with type cast'() {
    when:
    new Groovy1(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  def 'test groovy class forwarding to groovy class with type cast'() {
    when:
    new Groovy3(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  def 'test groovy class not forwarding'() {
    when:
    new Groovy4(null as String, verifier)

    then:
    1 * verifier.verify(String)
  }

  static class Groovy1 extends Java1 {
    Groovy1(String s, Verifier verifier) {
      super(s as String, verifier)
    }

    Groovy1(Pattern p, Verifier verifier) {
      super(p as Pattern, verifier)
    }
  }

  static abstract class Groovy2 {
    Groovy2(String s, Verifier verifier) {
      verifier.verify(String.class)
    }

    Groovy2(Pattern p, Verifier verifier) {
      verifier.verify(Pattern.class)
    }
  }

  static class Groovy3 extends Groovy2 {
    Groovy3(String s, Verifier verifier) {
      super(s as String, verifier)
    }

    Groovy3(Pattern p, Verifier verifier) {
      super(p as Pattern, verifier)
    }
  }

  static class Groovy4 {
    Groovy4(String s, Verifier verifier) {
      verifier.verify(String.class)
    }

    Groovy4(Pattern p, Verifier verifier) {
      verifier.verify(Pattern.class)
    }
  }
}
