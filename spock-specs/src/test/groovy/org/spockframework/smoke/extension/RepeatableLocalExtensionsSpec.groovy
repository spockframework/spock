/*
 * Copyright 2020 the original author or authors.
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
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.util.EmbeddedSpecRunner
import spock.util.mop.ConfineMetaClassChanges

import java.lang.annotation.Repeatable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@ConfineMetaClassChanges(EmbeddedSpecRunner)
class RepeatableLocalExtensionsSpec extends EmbeddedSpecification {
  def setup() {
    runner.addClassImport(Foo)
    runner.addClassImport(FooContainer)

    runner.metaClass.runSpec = {
      runner.runWithImports """
class FooSpec extends Specification {
  static specAnnotationsVisited = 0
  static featureAnnotationsVisited = 0
  static fixtureAnnotationsVisited = 0
  static fieldAnnotationsVisited = 0

  $it
}
"""
    }
  }

  def "repeated annotations work properly on spec"() {
    expect:
    runner.runWithImports """
$annotations
class FooSpec extends Specification {
  static specAnnotationsVisited = 0
  static featureAnnotationsVisited = 0
  static fixtureAnnotationsVisited = 0
  static fieldAnnotationsVisited = 0

  def foo() {
    expect:
    verifyAll {
      specAnnotationsVisited == $expected
      featureAnnotationsVisited == 0
      fixtureAnnotationsVisited == 0
      fieldAnnotationsVisited == 0
    }
  }
}
"""

    where:
    annotations                             || expected
    ''                                      || 0
    '@Foo'                                  || 1
    '@Foo @Foo'                             || 2
    '          @FooContainer([])'           || 0
    '@Foo      @FooContainer([])'           || 1
    '          @FooContainer(@Foo)'         || 1
    '          @FooContainer([@Foo])'       || 1
    '@Foo      @FooContainer(@Foo)'         || 2
    '@Foo      @FooContainer([@Foo])'       || 2
    '          @FooContainer([@Foo, @Foo])' || 2
    '@Foo      @FooContainer([@Foo, @Foo])' || 3
  }

  def "repeated annotations work properly on feature"() {
    expect:
    runner.runSpec """
$annotations
def foo() {
  expect:
  verifyAll {
    specAnnotationsVisited == 0
    featureAnnotationsVisited == $expected
    fixtureAnnotationsVisited == 0
    fieldAnnotationsVisited == 0
  }
}
"""

    where:
    annotations                             || expected
    ''                                      || 0
    '@Foo'                                  || 1
    '@Foo @Foo'                             || 2
    '          @FooContainer([])'           || 0
    '@Foo      @FooContainer([])'           || 1
    '          @FooContainer(@Foo)'         || 1
    '          @FooContainer([@Foo])'       || 1
    '@Foo      @FooContainer(@Foo)'         || 2
    '@Foo      @FooContainer([@Foo])'       || 2
    '          @FooContainer([@Foo, @Foo])' || 2
    '@Foo      @FooContainer([@Foo, @Foo])' || 3
  }

  def "repeated annotations work properly on fixture"() {
    expect:
    runner.runSpec """
$annotations
def setup() {
}

def foo() {
  expect:
  verifyAll {
    specAnnotationsVisited == 0
    featureAnnotationsVisited == 0
    fixtureAnnotationsVisited == $expected
    fieldAnnotationsVisited == 0
  }
}
"""

    where:
    annotations                             || expected
    ''                                      || 0
    '@Foo'                                  || 1
    '@Foo @Foo'                             || 2
    '          @FooContainer([])'           || 0
    '@Foo      @FooContainer([])'           || 1
    '          @FooContainer(@Foo)'         || 1
    '          @FooContainer([@Foo])'       || 1
    '@Foo      @FooContainer(@Foo)'         || 2
    '@Foo      @FooContainer([@Foo])'       || 2
    '          @FooContainer([@Foo, @Foo])' || 2
    '@Foo      @FooContainer([@Foo, @Foo])' || 3
  }

  def "repeated annotations work properly on field"() {
    expect:
    runner.runSpec """
$annotations
def foo

def foo() {
  expect:
  verifyAll {
    specAnnotationsVisited == 0
    featureAnnotationsVisited == 0
    fixtureAnnotationsVisited == 0
    fieldAnnotationsVisited == $expected
  }
}
"""

    where:
    annotations                             || expected
    ''                                      || 0
    '@Foo'                                  || 1
    '@Foo @Foo'                             || 2
    '          @FooContainer([])'           || 0
    '@Foo      @FooContainer([])'           || 1
    '          @FooContainer(@Foo)'         || 1
    '          @FooContainer([@Foo])'       || 1
    '@Foo      @FooContainer(@Foo)'         || 2
    '@Foo      @FooContainer([@Foo])'       || 2
    '          @FooContainer([@Foo, @Foo])' || 2
    '@Foo      @FooContainer([@Foo, @Foo])' || 3
  }

  static class FooExtension implements IAnnotationDrivenExtension<Foo> {
    @Override
    void visitSpecAnnotation(Foo annotation, SpecInfo spec) {
      spec.reflection.specAnnotationsVisited++
    }

    @Override
    void visitFeatureAnnotation(Foo annotation, FeatureInfo feature) {
      feature.spec.reflection.featureAnnotationsVisited++
    }

    @Override
    void visitFixtureAnnotation(Foo annotation, MethodInfo fixtureMethod) {
      fixtureMethod.parent.reflection.fixtureAnnotationsVisited++
    }

    @Override
    void visitFieldAnnotation(Foo annotation, FieldInfo field) {
      field.parent.reflection.fieldAnnotationsVisited++
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(RepeatableLocalExtensionsSpec.FooExtension)
@Repeatable(FooContainer)
@interface Foo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface FooContainer {
  Foo[] value();
}
