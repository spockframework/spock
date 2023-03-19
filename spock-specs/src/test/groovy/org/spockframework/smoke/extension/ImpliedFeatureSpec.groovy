/*
 * Copyright 2023 the original author or authors.
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

import org.junit.platform.engine.FilterResult
import org.junit.platform.launcher.PostDiscoveryFilter
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import static org.junit.platform.testkit.engine.EventConditions.displayName

class ImpliedFeatureSpec extends EmbeddedSpecification {
  def setup() {
    compiler.addClassImport(Implies)
    compiler.addClassImport(ImpliedBy)
  }

  def "implying a feature in the same specification"() {
    def clazz = compiler.compileSpecBody """
def foo() { expect: true }
@Implies('foo')
def bar() { expect: true }
def baz() { expect: true }
"""

    when:
    def result = runner.runClass(clazz, {
      FilterResult.includedIf(it.displayName == "bar")
    } as PostDiscoveryFilter)

    then:
    result.testEvents().succeeded().assertEventsMatchExactly(
      displayName("foo"),
      displayName("bar")
    )
  }

  def "implying a feature in a super specification"() {
    def clazz = compiler.compileWithImports("""
class Super extends Specification {
  def foo() { expect: true }
  def bam() { expect: true }
}
class Sub extends Super {
  @Implies('foo')
  def bar() { expect: true }
  def baz() { expect: true }
}
""")[1]

    when:
    def result = runner.runClass(clazz, {
      FilterResult.includedIf(it.displayName == "bar")
    } as PostDiscoveryFilter)

    then:
    result.testEvents().succeeded().assertEventsMatchExactly(
      displayName("foo"),
      displayName("bar")
    )
  }

  def "implying a feature in a sub specification"() {
    def clazz = compiler.compileWithImports("""
class Super extends Specification {
  def foo() { expect: true }
  def bam() { expect: true }
}
class Sub extends Super {
  @ImpliedBy('foo')
  def bar() { expect: true }
  def baz() { expect: true }
}
""")[1]

    when:
    def result = runner.runClass(clazz, {
      FilterResult.includedIf(it.displayName == "foo")
    } as PostDiscoveryFilter)

    then:
    result.testEvents().succeeded().assertEventsMatchExactly(
      displayName("foo"),
      displayName("bar")
    )
  }

  static class ImpliesExtension implements IAnnotationDrivenExtension<Implies> {
    @Override
    void visitFeatureAnnotation(Implies annotation, FeatureInfo feature) {
      feature.addImpliedFeature(feature.getParent().allFeatures.find {
        it.displayName == annotation.value()
      })
    }
  }

  static class ImpliedByExtension implements IAnnotationDrivenExtension<ImpliedBy> {
    @Override
    void visitFeatureAnnotation(ImpliedBy annotation, FeatureInfo feature) {
      feature.getParent().allFeatures.find {
        it.displayName == annotation.value()
      }.addImpliedFeature(feature)
    }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(ImpliedFeatureSpec.ImpliesExtension)
@interface Implies {
  String value()
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(ImpliedFeatureSpec.ImpliedByExtension)
@interface ImpliedBy {
  String value()
}
