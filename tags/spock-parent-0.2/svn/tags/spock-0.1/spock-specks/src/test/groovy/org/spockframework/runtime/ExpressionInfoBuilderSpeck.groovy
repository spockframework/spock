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

package org.spockframework.runtime

import org.junit.runner.RunWith
import org.spockframework.runtime.ExpressionInfoBuilder
import org.spockframework.runtime.ValueRecorder
import spock.lang.*
import org.spockframework.runtime.model.TextPosition
import org.spockframework.runtime.model.TextRegion
import org.spockframework.runtime.ValueRecorder

/**
 * A ...
 
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class ExpressionInfoBuilderSpeck {
  ValueRecorder recorder = new ValueRecorder();

  def setup() {
    for (i in 0..99) { recorder.record(i, null) }
  }

  def literal() {
    def expr = new ExpressionInfoBuilder("true", TextPosition.create(1, 1), recorder).build()
    expect:
      expr != null
      expr.region == TextRegion.create(TextPosition.create(1, 1), TextPosition.create(1, 5))
      expr.anchor == TextPosition.create(1, 1)
      expr.children.isEmpty()
      expr.text = "true"
      !expr.isRelevant()
  }

  def unaryExpr() {
    def expr = new ExpressionInfoBuilder("!foo", TextPosition.create(1, 1), recorder).build()
    expect:
      expr != null
      expr.region == TextRegion.create(TextPosition.create(1, 1), TextPosition.create(1, 5))
      expr.anchor == TextPosition.create(1, 1)
      expr.children.size() == 1
      expr.text == "!foo"
      expr.isRelevant()
  }

  def unaryExprOperand() {
    def expr = new ExpressionInfoBuilder("!foo", TextPosition.create(1, 1), recorder).build()
    def child = expr.children.get(0)
    expect:
      child != null
      child.region == TextRegion.create(TextPosition.create(1, 2), TextPosition.create(1, 5))
      child.anchor == TextPosition.create(1, 2)
      child.children.size() == 0
      child.text == "foo"
      child.isRelevant()
  }

  def binaryExpr() {
    def expr = new ExpressionInfoBuilder("1 > 2", TextPosition.create(1, 1), recorder).build()
    expect:
      expr != null
      expr.region == TextRegion.create(TextPosition.create(1, 1), TextPosition.create(1, 6))
      expr.anchor == TextPosition.create(1, 3)
      expr.children.size() == 2
      expr.text == "1 > 2"
      expr.isRelevant()
  }

  def methodCall() {
    def expr = new ExpressionInfoBuilder("foo.bar(42)", TextPosition.create(1, 1), recorder).build()
    expect:
      expr != null
      expr.region == TextRegion.create(TextPosition.create(1, 1), TextPosition.create(1, 12))
      expr.anchor == TextPosition.create(1, 5)
      expr.children.size() == 2
      expr.text == "foo.bar(42)"
      expr.isRelevant()
  }
}