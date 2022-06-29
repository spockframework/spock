/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException

import spock.lang.Issue

@Issue("http://my.issues.org/FOO-123")
class IssueExtension extends EmbeddedSpecification {
  def "spec-level @Issue annotation is converted to tag"() {
    def tags = specificationContext.currentSpec.tags

    expect:
    tags.size() == 1
    with(tags[0]) {
      name == "FOO-123"
      key == "issue"
      value == "FOO-123"
      url == "http://my.issues.org/FOO-123"
    }
  }

  @Issue("http://my.issues.org/FOO-456")
  def "feature-level @Issue annotation is converted to tag"() {
    def tags = specificationContext.currentFeature.tags

    expect:
    tags.size() == 1
    with(tags[0]) {
      name == "FOO-456"
      key == "issue"
      value == "FOO-456"
      url == "http://my.issues.org/FOO-456"
    }
  }

  @Issue(["http://my.issues.org/FOO-1", "http://my.issues.org/FOO-2"])
  def "if multiple issues are listed, all of them are converted to a tag"() {
    def tags = specificationContext.currentFeature.tags

    expect:
    tags.size() == 2
    with(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "http://my.issues.org/FOO-1"
    }
    with(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "http://my.issues.org/FOO-2"
    }
  }

  @Issue("http://my.issues.org/FOO-1")
  @Issue("http://my.issues.org/FOO-2")
  def "if multiple issue annotations are applied, all of them are converted to a tag"() {
    def tags = specificationContext.currentFeature.tags

    expect:
    tags.size() == 2
    with(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "http://my.issues.org/FOO-1"
    }
    with(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "http://my.issues.org/FOO-2"
    }
  }

  @Issue(["http://my.issues.org/FOO-1", "http://my.issues.org/FOO-2"])
  @Issue(["http://my.issues.org/FOO-3", "http://my.issues.org/FOO-4"])
  def "if multiple issue annotations with multiple issues are applied, all of them are converted to a tag"() {
    def tags = specificationContext.currentFeature.tags

    expect:
    tags.size() == 4
    with(tags[0]) {
      name == "FOO-1"
      key == "issue"
      value == "FOO-1"
      url == "http://my.issues.org/FOO-1"
    }
    with(tags[1]) {
      name == "FOO-2"
      key == "issue"
      value == "FOO-2"
      url == "http://my.issues.org/FOO-2"
    }
    with(tags[2]) {
      name == "FOO-3"
      key == "issue"
      value == "FOO-3"
      url == "http://my.issues.org/FOO-3"
    }
    with(tags[3]) {
      name == "FOO-4"
      key == "issue"
      value == "FOO-4"
      url == "http://my.issues.org/FOO-4"
    }
  }

  def "complains if @Issue is used on anything other than a spec or feature"() {
    when:
    runner.runSpecBody(
        """
@Issue("http://foo")
def setup() {}

def feature() {
  expect: true
}
""")

    then:
    InvalidSpecException e = thrown()
    e.message.contains("may not be applied to fixture methods")
  }
}
