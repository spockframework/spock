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

package org.spockframework.smoke;

import org.spockframework.EmbeddedSpecification

class FixtureMethods extends EmbeddedSpecification {
  static log

  def setup() {
    log = []
  }

  def "are run in correct order"() {
    when:
    runner.runSpecBody """
def getLog() { org.spockframework.smoke.FixtureMethods.log }

def setup() { log << "s" }
def cleanup() { log << "c" }
def setupSpec() { log << "ss" }
def cleanupSpec() { log << "cs" }

def feature() { expect: true }
    """

    then:
    log == ["ss", "s", "c", "cs"]
  }

  def "are run in correct order across class hierarchy"() {
    when:
    runner.runWithImports """
@Ignore
class Base extends Specification {
  def getLog() { org.spockframework.smoke.FixtureMethods.log }

  def setup() { log << "s1" }
  def cleanup() { log << "c1" }
  def setupSpec() { log << "ss1" }
  def cleanupSpec() { log << "cs1" }
}

class Derived extends Base {
  def getLog() { org.spockframework.smoke.FixtureMethods.log }

  def setup() { log << "s2" }
  def cleanup() { log << "c2" }
  def setupSpec() { log << "ss2" }
  def cleanupSpec() { log << "cs2" }

  def feature() { expect: true }
}
    """

    then:
    log == ["ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1"]
  }

  def "deprecated fixture methods still work"() {
    when:
    runner.runSpecBody """
def getLog() { org.spockframework.smoke.FixtureMethods.log }

def setupSpeck() { log << "ss" }
def cleanupSpeck() { log << "cs" }
def feature() { expect: true }
    """

    then:
    log == ["ss", "cs"]
  }
}
