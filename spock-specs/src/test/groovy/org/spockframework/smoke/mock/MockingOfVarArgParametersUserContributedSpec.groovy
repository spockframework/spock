
/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import spock.lang.*

@Issue("https://github.com/spockframework/spock/issues/177")
class MockingOfVarArgParametersUserContributedSpec extends Specification {
	def methodWasCalled = false

	TargetClass target = Mock()

	def "1-arg call on method supporting >= 1 params is matched with (_)"() {
		given:
	  target.oneOrGreater(_) >> { methodWasCalled = true }

		when:
		target.oneOrGreater("one")

		then:
		methodWasCalled
	}

	def "1-arg call on method supporting >= 0 params is matched with (_)"() {
		given:
		target.zeroOrGreater(_) >> { methodWasCalled = true }

		when:
		target.zeroOrGreater("one")

		then:
		methodWasCalled
	}

  interface TargetClass {
    void oneOrGreater(String first, String... rest)
    void zeroOrGreater(String... all)
  }
}



