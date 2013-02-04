/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
