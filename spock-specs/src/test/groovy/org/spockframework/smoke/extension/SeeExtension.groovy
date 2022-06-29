package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException

import spock.lang.See

@See("https://spockframework.org/spec")
class SeeExtension extends EmbeddedSpecification {
  def "adds attachment for spec-level @See annotation"() {
    def attachments = specificationContext.currentSpec.attachments

    expect:
    attachments.size() == 1
    attachments[0].name == "https://spockframework.org/spec"
    attachments[0].url == "https://spockframework.org/spec"
  }

  @See("https://spockframework.org/feature")
  def "adds attachment for feature-level @See annotation"() {
    def attachments = specificationContext.currentFeature.attachments

    expect:
    attachments.size() == 1
    attachments[0].name == "https://spockframework.org/feature"
    attachments[0].url == "https://spockframework.org/feature"
  }

  @See(["https://spockframework.org/one", "https://spockframework.org/two"])
  def "adds attachment for each URL"() {
    def attachments = specificationContext.currentFeature.attachments

    expect:
    attachments.size() == 2
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
  }

  @See("https://spockframework.org/one")
  @See("https://spockframework.org/two")
  def "adds attachment for each URL, if multiple see annotations are applied"() {
    def attachments = specificationContext.currentFeature.attachments

    expect:
    attachments.size() == 2
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
  }

  @See(["https://spockframework.org/one", "https://spockframework.org/two"])
  @See(["https://spockframework.org/three", "https://spockframework.org/four"])
  def "adds attachment for each URL, if multiple see annotations with multiple values are applied"() {
    def attachments = specificationContext.currentFeature.attachments

    expect:
    attachments.size() == 4
    attachments[0].name == "https://spockframework.org/one"
    attachments[0].url == "https://spockframework.org/one"
    attachments[1].name == "https://spockframework.org/two"
    attachments[1].url == "https://spockframework.org/two"
    attachments[2].name == "https://spockframework.org/three"
    attachments[2].url == "https://spockframework.org/three"
    attachments[3].name == "https://spockframework.org/four"
    attachments[3].url == "https://spockframework.org/four"
  }

  def "barks if @See is used on anything other than a spec or feature"() {
    when:
    runner.runSpecBody(
"""
@See("https://foo")
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
