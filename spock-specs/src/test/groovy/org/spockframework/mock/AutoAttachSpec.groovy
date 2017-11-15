/*
 * Copyright 2017 the original author or authors.
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
 *
 */

package org.spockframework.mock

import spock.lang.Specification
import spock.mock.AutoAttach
import spock.mock.DetachedMockFactory

class AutoAttachSpec extends Specification {

  DetachedMockFactory mockFactory = new DetachedMockFactory()

  @AutoAttach
  List<String> mock = mockFactory.Mock(List)

  def "Auto attaches mock to spec"() {
    when:
    mock.add("foo")

    then:
    1 * mock.add(_)
  }
}
