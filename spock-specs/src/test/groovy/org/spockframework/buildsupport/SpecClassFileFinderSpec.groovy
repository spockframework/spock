
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

package org.spockframework.buildsupport

import org.spockframework.util.Util

import spock.lang.*

class SpecClassFileFinderSpec extends Specification {
  def finder = new SpecClassFileFinder()

  def "class file of regular class isn't considered a runnable spec"() {
    expect:
    !finder.isRunnableSpec(Util.getClassFile(RegularClass))
  }

  def "class file of abstract spec isn't considered a runnable spec"() {
    expect:
    !finder.isRunnableSpec(Util.getClassFile(AbstractSpec))
  }

  def "class file of concrete spec is considered a runnable spec"() {
    expect:
    finder.isRunnableSpec(Util.getClassFile(ConcreteSpec))
  }
}

private class RegularClass {}

private abstract class AbstractSpec extends Specification {}

@Ignore
private class ConcreteSpec extends Specification {}
