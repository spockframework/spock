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

package org.spockframework.runtime.condition

import org.spockframework.util.Tuple2
import spock.lang.Specification

class EditDistanceSpec extends Specification {
  EditDistance dist

  def "compute distance"() {
    dist = new EditDistance("asdf", str)

    expect:
    dist.distance == d

    where:
    str << ["xsdf", "axdf", "asxf", "asdx", "", "a", "as", "asd", "asdf", "xasdf", "asdfx", "xasdfx"]
    d   << [ 1    , 1     , 1     , 1     , 4 , 3  , 2   , 1    , 0     , 1      , 1      , 2       ]
  }

  def "visualize distance"() {
    dist = new EditDistance("the quick", str2)

    when:
    Tuple2 result = dist.showDistance()

    then:
    result.get0() == res1
    result.get1() == res2

    where:
    str2 << ["the quirk"  , "quick"      , "e qui"        , "and now for sth. completely different"]
    res1 << ["the qui(c)k", "(the )quick", "(th)e qui(ck)", "()th()e() (quick)"]
    res2 << ["the qui(r)k", "()quick"    , "()e qui()"    , "(and now for s)th(. compl)e(tely) (different)"]
  }
}
