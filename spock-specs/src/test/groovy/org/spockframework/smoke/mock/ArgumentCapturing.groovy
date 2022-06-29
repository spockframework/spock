/*
 * Copyright 2013 the original author or authors.
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
package org.spockframework.smoke.mock

import spock.lang.Shared
import spock.lang.Specification

class ArgumentCapturing extends Specification {
  String instanceField

  @Shared
  String sharedField

  def "capture argument in local variable"() {
    List list = Mock()
    def local

    when:
    list.add("foo")

    then:
    list.add(_) >> { String elem -> local = elem }
    local == "foo"
  }

  def "capture argument in instance field"() {
    List list = Mock()

    when:
    list.add("foo")

    then:
    list.add(_) >> { String elem -> instanceField = elem }
    instanceField == "foo"
  }

  def "capture argument in shared field"() {
    List list = Mock()

    when:
    list.add("foo")

    then:
    list.add(_) >> { String elem -> sharedField = elem }
    sharedField == "foo"
  }
}
