/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import spock.lang.Specification

/**
 *
 * @author Peter Niederwieser
 */
class InteractionsAndExceptionConditions extends Specification {
  def "interaction followed by exception condition"() {
    List list = Mock()

    when:
    list.add(1)
    throw new RuntimeException()

    then:
    1 * list.add(1)
    thrown(RuntimeException)
  }

  def "exception condition followed by interaction"() {
    List list = Mock()

    when:
    list.add(1)
    throw new RuntimeException()

    then:
    thrown(RuntimeException)
    1 * list.add(1)
  }
}
