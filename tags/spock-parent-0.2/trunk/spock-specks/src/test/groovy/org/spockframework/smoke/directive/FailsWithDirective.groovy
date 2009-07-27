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

import org.junit.runner.RunWith
import spock.lang.*

/**
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class FailsWithDirectiveOnMethod {
  @FailsWith(IndexOutOfBoundsException)
  def ex1() {
    given:
    def foo = []
    foo.get(0)
  }

  @FailsWith(Exception)
  def ex2() {
    given:
    def foo = []
    foo.get(0)
  }

  def ex3() {
    expect: true
  }
}

@Speck
@RunWith(Sputnik)
@FailsWith(IndexOutOfBoundsException)
class FailsWithDirectiveOnSpeck {
  def ex1() {
    given:
    def foo = []
    foo.get(0)
  }

  def ex2() {
    given:
    def foo = []
    foo.get(1)
  }

  @FailsWith(NullPointerException)
  def ex3() {
    given:
    null.foo()
  }
}

