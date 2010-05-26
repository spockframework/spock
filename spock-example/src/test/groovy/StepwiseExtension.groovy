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

import spock.lang.*

/**
 * Demonstrates how &#64;Stepwise causes a spec to be run in incremental steps.
 * Change a step's condition from <tt>true</tt> to <tt>false</tt>, and observer
 * how the remaining steps will be skipped automatically on the next run.
 * Also notice that if you run a single step (e.g. from the IDE's context menu),
 * all prior steps will also be run.
 *
 * <p><tt>&#64;Stepwise</tt> is particularly useful for higher-level specs whose
 * methods have logical dependencies.
 *
 * @since 0.4
 */
@Stepwise
class StepwiseExtension extends Specification {
  def "step 1"() {
    expect: true // try to change this to 'false'
  }

  def "step 2"() {
    expect: true
  }

  def "step 3"() {
    expect: true
  }
}
