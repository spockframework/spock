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
  
package org.spockframework.runtime.condition;

import spock.lang.*

class StringDifferenceRendererSpec extends Specification {
  def renderer = new StringDifferenceRenderer()

  def "examples"() {
    def matrix = new StringDistanceMatrix(str1, str2)

    expect:
    renderer.render(str1, str2, matrix.computePath()) == "$out1\n$out2"

    where:
    str1 = "the quick"
    str2 << ["the quirk"  , "quick"      , "e qui"        , "and now for sth. completely different"]
    out1 << ["the qui(c)k", "(the )quick", "(th)e qui(ck)", "(-------------)th(-------)e(----) (qu)i(ck-----)"]
    out2 << ["the qui(r)k", "(----)quick", "(--)e qui(--)", "(and now for s)th(. compl)e(tely) (d-)i(fferent)"]
  }

  def "special characters in original strings"() {
    def matrix = new StringDistanceMatrix(str1, str2)

    expect:
    renderer.render(str1, str2, matrix.computePath()) == "$out1\n$out2"
    
    where:
    str1 = "one\ttwothree\bfour\rfive\fsix"
    str2 = "onetwo\nthreefour\rfive\tsix"
    out1 = "one(\\t)two(~~)three(\\b)four\\rfive(\\f)six"
    out2 = "one(~~)two(\\n)three(~~)four\\rfive(\\t)six"
  }

  def "delimiters in original strings"() {
    def matrix = new StringDistanceMatrix(str1, str2)

    expect:
    renderer.render(str1, str2, matrix.computePath()) == "$out1\n$out2"

    where:
    str1 = "q(u)i(c)k("
    str2 = "q(u)i(r)k)"
    out1 = "q(u)i((c))k(()"
    out2 = "q(u)i((r))k())"
  }
}
