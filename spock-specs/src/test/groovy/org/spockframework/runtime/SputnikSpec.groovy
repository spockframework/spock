package org.spockframework.runtime

import org.junit.runner.manipulation.Filter
import org.junit.runner.Description
import org.junit.runner.manipulation.Sorter

import spock.lang.Specification

class SputnikSpec extends Specification {
  def "description reflects subsequent filtering"() {
    def sputnik = new Sputnik(SputnikSampleSpec)

    expect:
    sputnik.description.children.methodName == ["feature1", "feature2", "feature3"]

    when:
    sputnik.filter(new Filter() {
      @Override
      boolean shouldRun(Description description) {
        description.methodName == "feature2"
      }
      @Override
      String describe() {
        "filter"
      }
    })

    then:
    sputnik.description.children.methodName == ["feature2"]
  }

  def "description reflects subsequent sorting"() {
    def sputnik = new Sputnik(SputnikSampleSpec)

    expect:
    sputnik.description.children.methodName == ["feature1", "feature2", "feature3"]

    when:
    sputnik.sort(new Sorter(new Comparator<Description>() {
      int compare(Description desc1, Description desc2) {
        desc2.methodName <=> desc1.methodName
      }
    }))

    then:
    sputnik.description.children.methodName == ["feature3", "feature2", "feature1"]
  }
}

class SputnikSampleSpec extends Specification {
  def feature1() {
    expect: true
  }

  def feature2() {
    expect: true
  }

  def feature3() {
    expect: true
  }
}
