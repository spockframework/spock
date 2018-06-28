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

import org.spockframework.runtime.model.Attachment
import org.spockframework.runtime.model.BlockInfo
import org.spockframework.runtime.model.BlockKind
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.Tag
import org.spockframework.util.ExceptionUtil

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ReportLogEmitterSpec extends Specification {

  @Shared
  def featureWithoutIteration = new FeatureInfo()
  @Shared
  def featureWithIteration = new FeatureInfo()

  def spec = new SpecInfo()
  def iteration = new IterationInfo(featureWithIteration, null, 1)
  def log
  def listener = new IReportLogListener() {
    void emitted(Map<String, Object> log) {
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
      addFeature(featureWithoutIteration)
    }

    featureWithoutIteration.parent = spec // for some reason doesn't work inside `with` block
    prepareFeature(featureWithoutIteration)

    featureWithIteration.parent = spec
    prepareFeature(featureWithIteration)
    featureWithIteration.dataProcessorMethod = new MethodInfo()
    featureWithIteration.reportIterations = true

    iteration.name = "sample feature[0]"
  }

  def cleanup() {
    featureWithoutIteration = new FeatureInfo()
    featureWithIteration = new FeatureInfo()
  }

  private Object prepareFeature(FeatureInfo featureInfo) {
    featureInfo.with {
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

  @Unroll
  def "feature started (#name)"(String name, FeatureInfo feature) {
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

    where:
    name                      | feature
    "featureWithoutIteration" | featureWithoutIteration
    "featureWithIteration"    | featureWithIteration
  }

  def "iteration started"() {
    when:
    emitter.beforeIteration(iteration)

    then:
    log == [
      package: "foo.bar",
      name: "SampleSpec",
      features: [[
       name: "sample feature",
       iterations: [[
          name: "sample feature[0]",
          start: 123456789,
       ]]
     ]]
    ]
  }

  def "iteration completed"() {
    when:
    emitter.afterIteration(iteration)

    then:
    log == [
      package: "foo.bar",
      name: "SampleSpec",
      features: [[
         name: "sample feature",
         iterations: [[
            name: "sample feature[0]",
            end: 123456789,
            result: "passed"
          ]]
       ]]
    ]
  }


  @Unroll
  def "feature completed (#name)"(String name, FeatureInfo feature) {
    when:
    emitter.afterFeature(featureWithoutIteration)

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

    where:
    name                      | feature
    "featureWithoutIteration" | featureWithoutIteration
    "featureWithIteration"    | featureWithIteration
  }

  @Unroll
  def "feature skipped (#name)"(String name, FeatureInfo feature) {
    when:
    emitter.featureSkipped(featureWithoutIteration)

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

    where:
    name                      | feature
    "featureWithoutIteration" | featureWithoutIteration
    "featureWithIteration"    | featureWithIteration
  }

  def "failure during feature execution (featureWithoutIteration)"() {
    def method = new MethodInfo()
    method.feature = featureWithoutIteration
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

  def "failure during feature execution (featureWithIteration)"() {
    def method = new MethodInfo()
    method.feature = featureWithIteration
    method.parent = spec
    method.iteration = iteration

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
         iterations: [[
           name: "sample feature[0]",
           exceptions: [
             ExceptionUtil.printStackTrace(exception)
           ]
         ]]
       ]]
    ]
  }

  def "standard out during feature execution (featureWithoutIteration)"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(featureWithoutIteration)
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

  def "standard out during feature execution (featureWithIteration)"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(featureWithIteration)
    emitter.beforeIteration(iteration)
    emitter.standardOut("foo\nbar")

    then:
    log == [
      package: "foo.bar",
      name: "SampleSpec",
      features: [[
         name: "sample feature",
         iterations: [[
            name: "sample feature[0]",
            output: ["foo\nbar"]
          ]]
       ]]
    ]
  }

  def "standard err during feature execution (featureWithoutIteration)"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(featureWithoutIteration)
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

  def "standard err during feature execution (featureWithIteration)"() {
    when:
    emitter.beforeSpec(spec)
    emitter.beforeFeature(featureWithIteration)
    emitter.beforeIteration(iteration)
    emitter.standardErr("foo\nbar")

    then:
    log == [
      package: "foo.bar",
      name: "SampleSpec",
      features: [[
         name: "sample feature",
         iterations: [[
              name: "sample feature[0]",
              errorOutput: ["foo\nbar"]
            ]]
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
