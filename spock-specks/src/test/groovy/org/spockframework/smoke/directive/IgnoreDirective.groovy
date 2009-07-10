/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.directive

import spock.lang.*
import org.junit.runner.RunWith
import org.junit.runner.JUnitCore
import org.spockframework.runtime.InvalidSpeckError
import org.spockframework.smoke.EmbeddedSpeckRunner
import org.spockframework.smoke.EmbeddedSpeckRunner
import org.spockframework.smoke.EmbeddedSpeckRunner

/**
 *
 * @author Peter Niederwieser
 */
@Issue("12")
@Ignore
@Speck
@RunWith(Sputnik)
class IgnoreSpeck {
  def "ignored 1"() {
    expect: false
  }

  def "ignored 2"() {
    expect: false
  }
}

@Speck
@RunWith(Sputnik)
class IgnoreFeatureMethods {
  @Ignore
  def "ignored 1"() {
    expect: false
  }

  @Ignore
  def "ignored 2"() {
    expect: false
  }

  def "not ignored"() {
    expect: true
  }
}

@Speck
@RunWith(Sputnik)
class IgnoreFixtureMethods {
  def "fixture methods cannot be ignored"() {
    def runner = new EmbeddedSpeckRunner()

    when:
    runner.runSpeckBody """
@Ignore
def setup() {}
    """

    then:
    thrown(InvalidSpeckError)
  }
}



