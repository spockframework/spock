/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.docs.primer

import org.opentest4j.MultipleFailuresError
import org.spockframework.smoke.condition.ConditionRenderingSpec
import spock.lang.Snapshot
import spock.lang.Snapshotter

class VerifyEachDocSpec extends ConditionRenderingSpec {

  def "simple list equality"() {
    expect:
    isRendered(/* tag::simple-list-equality-result[] */ """
list == [1, 2, 4]
|    |
|    false
[1, 2, 3]
""", // end::simple-list-equality-result[]
      {
        // tag::simple-list-equality[]
        def list = [1, 2, 3]
        assert list == [1, 2, 4]
        // end::simple-list-equality[]
      }
    )
  }

  def "simple set equality"() {
    expect:
    isRendered(/* tag::simple-set-equality-result[] */ """
set == [1, 2, 4] as Set
|   |
|   false
|   2 differences (66% similarity, 1 missing, 1 extra)
|   missing: [4]
|   extra: [3]
[2, 3, 1]
""", // end::simple-set-equality-result[]
      {
        // tag::simple-set-equality[]
        def set = [2, 3, 1] as Set
        assert set == [1, 2, 4] as Set
        // end::simple-set-equality[]
      }
    )
  }

  def "every method"() {
    expect:
    isRendered(/* tag::every-method-result[] */ """
list.every { it == 2 }
|    |
|    false
[1, 2, 3]
""", // end::every-method-result[]
      {
        // tag::every-method[]
        def list = [1, 2, 3]
        assert list.every { it == 2 }
        // end::every-method[]
      }
    )
  }

  def "verifyEach method"(@Snapshot Snapshotter snap) {
    when:
    // tag::verify-each[]
    def list = [1, 2, 3]
    verifyEach(list) { it == 2 }
    // end::verify-each[]
    then:
    MultipleFailuresError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }
  def "verifyEach with namer method"(@Snapshot Snapshotter snap) {
    when:
    // tag::verify-each-namer[]
    def list = [1, 2, 3]
    verifyEach(list, { "int($it)" }) { it == 2 }
    // end::verify-each-namer[]
    then:
    MultipleFailuresError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }
}
