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

import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.Attachment
import org.spockframework.runtime.model.BlockInfo
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.Tag
import org.spockframework.util.ExceptionUtil
import org.spockframework.util.GroovyUtil
import org.spockframework.util.IStandardStreamListener

// NOTE: assumes single-threaded execution
// TODO: challenge assumptions that tags are added before execution, and attachments afterwards
class ReportLogEmitter extends AbstractRunListener implements IStandardStreamListener {
  private final IReportLogListener listener

  private SpecInfo currentSpec
  private FeatureInfo currentFeature

  private boolean specFailed
  private boolean featureFailed

  ReportLogEmitter(IReportLogListener listener) {
    this.listener = listener
  }

  void standardOut(Object object) {
    standardStream(object, "output")
  }

  void standardErr(Object object) {
    standardStream(object, "errorOutput")
  }

  private void standardStream(Object object, String key) {
    if (currentFeature) {
      emit([
          package: currentSpec.package,
          name: currentSpec.name,
          features: [
              [
                  name: currentFeature.name,
                  (key): [object.toString()]
              ]
          ]
      ])
    } else {
      emit([
          package: currentSpec.package,
          name: currentSpec.name,
          (key): [object.toString()]
      ])
    }
  }

  @Override
  void beforeSpec(SpecInfo spec) {
    currentSpec = spec
    specFailed = false

    emit([
        package: spec.package,
        name: spec.name,
        *: renderNarrative(spec.narrative),
        *: renderTags(spec.tags),
        start: getCurrentTime()
    ])
  }

  @Override
  void beforeFeature(FeatureInfo feature) {
    currentFeature = feature
    featureFailed = false

    emit([
        package: feature.spec.bottomSpec.package,
        name: feature.spec.bottomSpec.name,
        features: [
            [
                name: feature.name,
                *: renderBlocks(feature.blocks),
                *: renderTags(feature.tags),
                start: getCurrentTime()
            ]
        ]
    ])
  }

  @Override
  void afterFeature(FeatureInfo feature) {
    emit([
        package: feature.spec.bottomSpec.package,
        name: feature.spec.bottomSpec.name,
        features: [
            [
                name: feature.name,
                end: getCurrentTime(),
                result: featureFailed ? "failed" : "passed",
                *: renderAttachments(feature.attachments)
            ]
        ]
    ])

    currentFeature = null
  }

  @Override
  void afterSpec(SpecInfo spec) {
    emit([
        package: spec.package,
        name: spec.name,
        end: getCurrentTime(),
        result: specFailed ? "failed" : "passed",
        *: renderAttachments(spec.attachments)
    ])

    currentSpec = null
  }

  @Override
  void error(ErrorInfo error) {
    specFailed = true
    def spec = error.method.parent.bottomSpec
    def feature = error.method.feature
    if (feature) {
      featureFailed = true
      emit([
          package: spec.package,
          name: spec.name,
          features: [
              [
                  name: feature.name,
                  exceptions: [
                      ExceptionUtil.printStackTrace(error.exception)
                  ]
              ]
          ]
      ])
    } else {
      emit([
          package: spec.package,
          name: spec.name,
          exceptions: [
              ExceptionUtil.printStackTrace(error.exception)
          ]
      ])
    }
  }

  @Override
  void specSkipped(SpecInfo spec) {
    def now = getCurrentTime()

    emit([
        package: spec.package,
        name: spec.name,
        *: renderNarrative(spec.narrative),
        *: renderTags(spec.tags),
        start: now,
        end: now,
        result: "skipped"
    ])
  }

  @Override
  void featureSkipped(FeatureInfo feature) {
    def now = getCurrentTime()

    emit([
        package: feature.spec.bottomSpec.package,
        name: feature.spec.bottomSpec.name,
        features: [
            [
                name: feature.name,
                *: renderBlocks(feature.blocks),
                *: renderTags(feature.tags),
                start: now,
                end: now,
                result: "skipped"
            ]
        ]
    ])
  }

  protected long getCurrentTime() {
    System.currentTimeMillis()
  }

  private Object renderTags(List<Tag> tags) {
    def result = tags.collect {
      GroovyUtil.filterNullValues([
        name: it.name,
        key: it.key,
        value: it.value,
        url: it.url
      ])
    }
    result ? [tags: result] : Collections.emptyMap()
  }

  private Object renderNarrative(String narrative) {
    narrative ? [narrative: narrative] : Collections.emptyMap()
  }

  private Object renderBlocks(List<BlockInfo> blocks) {
    def result = blocks.collectMany {
      if (it.texts.empty) return Collections.emptyList()

      def name = it.kind.name()
      def label = name == "SETUP" ? "Given" : name.toLowerCase().capitalize()
      def labels = [label] + ["And"] * (it.texts.size() -1)
      [labels, it.texts].transpose().collect { lbl, text -> lbl + " " + text }
    }.join("\n")

    result ? [narrative: result] : Collections.emptyMap()
  }

  private Object renderAttachments(List<Attachment> attachments) {
    def result = attachments.collect {[
        name: it.name,
        url: it.url
    ]}
    result ? [attachments: result] : Collections.emptyMap()
  }

  private void emit(Map log) {
    listener.emitted(log)
  }
}
