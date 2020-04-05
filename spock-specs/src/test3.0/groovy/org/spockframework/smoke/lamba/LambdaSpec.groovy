package org.spockframework.smoke.lamba

import spock.lang.Specification

import static java.util.Comparator.comparingInt
import static java.util.stream.Collectors.toList

class LambdaSpec extends Specification {

  private List<String> names = ["Fred", "Wilma", "Barney", "Betty"]

  def "allow to use Java lambda in spec"() {
    when:
    List<String> longNames = names.stream().filter(name -> name.length() > 5).collect(toList())

    then:
    longNames == ["Barney"]
  }

  def "allow to use method reference in spec"() {
    when:
    List<String> namesFromShortest = names.stream().sorted(comparingInt(String::length).reversed()).collect(toList())

    then:
    namesFromShortest == ["Barney", "Wilma", "Betty", "Fred"]
  }
}
