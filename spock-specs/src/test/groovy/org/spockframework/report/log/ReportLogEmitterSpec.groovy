/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.report.log

import org.spockframework.runtime.model.*
import org.spockframework.util.ExceptionUtil

import spock.lang.Specification

class ReportLogEmitterSpec extends Specification {
  def spec = new SpecInfo()
  def feature = new FeatureInfo()
  def log
  def listener = new IReportLogListener() {
    void emitted(Map log) {
      this.log = log
    }
  }
  def emitter = new ReportLogEmitter() {
    @Override
    protected long getCurrentTime() {
      123456789
    }
  }

  def setup() {
    emitter.addListener(listener)

    spec.with {
      setPackage("foo.bar")
      name = "SampleSpec"
      narrative = "As a user\nI want foo\nSo that bar"
      addTag(new Tag("tag name", "tag key", "tag value", "http://tag.url"))
      addAttachment(new Attachment("att name", "http://att.url"))
      addFeature(feature)
    }

    feature.parent = spec // for some reason doesn't work inside `with` block
    feature.with {
      name = "sample feature"
      addTag(new Tag("ftag name", "ftag key", "ftag value", "http://ftag.url"))
      addAttachment(new Attachment("fatt name", "http://fatt.url"))

      def given = new BlockInfo()
      given.with {
        kind = BlockKind.SETUP
        texts = ["foo"]
      }
      addBlock(given)

      def when = new BlockInfo()
      when.with {
        kind = BlockKind.WHEN
        texts = ["bar"]
      }
      addBlock(when)

      def then = new BlockInfo()
      then.with {
        kind = BlockKind.THEN
        texts = ["baz", "bazbaz"]
      }
      addBlock(then)
    }
  }

  def "spec started"() {
    when:
    emitter.beforeSpec(spec)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        start: 123456789,
        narrative: "As a user\nI want foo\nSo that bar",
        tags: [[name: "tag name", key: "tag key", value: "tag value", url: "http://tag.url"]]

    ]
  }

  def "spec completed"() {
    when:
    emitter.afterSpec(spec)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        end: 123456789,
        result: "passed",
        attachments: [[name: "att name", url: "http://att.url"]]
    ]
  }

  def "spec skipped"() {
    when:
    emitter.specSkipped(spec)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        start: 123456789,
        end: 123456789,
        result: "skipped",
        narrative: "As a user\nI want foo\nSo that bar",
        tags: [[name: "tag name", key: "tag key", value: "tag value", url: "http://tag.url"]]
    ]
  }

  def "failure during spec execution"() {
    def method = new MethodInfo()
    method.feature = null
    method.parent = spec

    def exception = new Exception("ouch")
    def error = new ErrorInfo(method, exception)

    when:
    emitter.error(error)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        exceptions: [
          ExceptionUtil.printStackTrace(exception)
        ]
    ]
  }

  def "feature started"() {
    when:
    emitter.beforeFeature(feature)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            start: 123456789,
            tags: [[name: "ftag name", key: "ftag key", value: "ftag value", url: "http://ftag.url"]],
            narrative: "Given foo\nWhen bar\nThen baz\nAnd bazbaz"
        ]]
    ]
  }

  def "feature completed"() {
    when:
    emitter.afterFeature(feature)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            end: 123456789,
            attachments: [[name: "fatt name", url: "http://fatt.url"]],
            result: "passed"
        ]]
    ]
  }

  def "feature skipped"() {
    when:
    emitter.featureSkipped(feature)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            start: 123456789,
            end: 123456789,
            narrative: "Given foo\nWhen bar\nThen baz\nAnd bazbaz",
            tags: [[name: "ftag name", key: "ftag key", value: "ftag value", url: "http://ftag.url"]],
            result: "skipped"
        ]]
    ]
  }

  def "failure during feature execution"() {
    def method = new MethodInfo()
    method.feature = feature
    method.parent = spec

    def exception = new Exception("ouch")
    def error = new ErrorInfo(method, exception)

    when:
    emitter.error(error)

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            exceptions: [
                ExceptionUtil.printStackTrace(exception)
            ]
        ]]
    ]
  }

  def "standard out during feature execution"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(feature)
    emitter.standardOut("foo\nbar")

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            output: ["foo\nbar"]
        ]]
    ]
  }

  def "standard err during feature execution"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(feature)
    emitter.standardErr("foo\nbar")

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        features: [[
            name: "sample feature",
            errorOutput: ["foo\nbar"]
        ]]
    ]
  }

  def "standard out during spec execution"() {
    when:
    emitter.beforeSpec(spec)
    emitter.standardOut("foo\nbar")

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        output: ["foo\nbar"]
    ]
  }

  def "standard err during spec execution"() {
    when:
    emitter.beforeSpec(spec)
    emitter.standardErr("foo\nbar")

    then:
    log == [
        package: "foo.bar",
        name: "SampleSpec",
        errorOutput: ["foo\nbar"]
    ]
  }
}
