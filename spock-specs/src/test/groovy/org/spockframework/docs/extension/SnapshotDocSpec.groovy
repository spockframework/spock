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

package org.spockframework.docs.extension

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import spock.lang.Snapshot
import spock.lang.Snapshotter
import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo

class SnapshotDocSpec extends Specification {
  // tag::field[]
  @Snapshot
  Snapshotter snapshotter

  def "using field based snapshot"() {
    expect:
    snapshotter.assertThat("from field").matchesSnapshot()
  }
  // end::field[]


  // tag::parameter[]
  def "using parameter based snapshot"(@Snapshot Snapshotter snapshotter) {
    expect:
    snapshotter.assertThat("from parameter").matchesSnapshot()
  }
  // end::parameter[]

  // tag::multi-snapshot[]
  def "multi snapshot"() {
    expect:
    snapshotter.assertThat("from parameter").matchesSnapshot()
    snapshotter.assertThat("other parameter").matchesSnapshot("otherId")
  }
  // end::multi-snapshot[]

  // tag::custom-matching[]
  def "custom matching"() {
    expect:
    snapshotter.assertThat("data").matchesSnapshot { snapshot, actual ->
      /* Hamcrest matchers */
      assertThat(actual, equalTo(snapshot))
    }
  }
  // end::custom-matching[]
}
