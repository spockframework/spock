/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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

  def "should work properly on spec"() {
    expect:
    runner.runWithImports """
${buildContainer(contained, containerWithList)}
${'@Foo\n' * individual}
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
    individual | contained | containerWithList || expected
    0          | -1        | false             || 0
    1          | -1        | false             || 1
    2          | -1        | false             || 2
    0          | 0         | false             || 0
    0          | 0         | true              || 0
    1          | 0         | false             || 1
    1          | 0         | true              || 1
    0          | 1         | false             || 1
    0          | 1         | true              || 1
    1          | 1         | false             || 2
    1          | 1         | true              || 2
    0          | 2         | true              || 2
    1          | 2         | true              || 3
  }

  def "should work properly on feature"() {
    expect:
    runner.runSpec """
${buildContainer(contained, containerWithList)}
${'@Foo\n' * individual}
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
    individual | contained | containerWithList || expected
    0          | -1        | false             || 0
    1          | -1        | false             || 1
    2          | -1        | false             || 2
    0          | 0         | false             || 0
    0          | 0         | true              || 0
    1          | 0         | false             || 1
    1          | 0         | true              || 1
    0          | 1         | false             || 1
    0          | 1         | true              || 1
    1          | 1         | false             || 2
    1          | 1         | true              || 2
    0          | 2         | true              || 2
    1          | 2         | true              || 3
  }

  def "should work properly on fixture"() {
    expect:
    runner.runSpec """
${buildContainer(contained, containerWithList)}
${'@Foo\n' * individual}
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
    individual | contained | containerWithList || expected
    0          | -1        | false             || 0
    1          | -1        | false             || 1
    2          | -1        | false             || 2
    0          | 0         | false             || 0
    0          | 0         | true              || 0
    1          | 0         | false             || 1
    1          | 0         | true              || 1
    0          | 1         | false             || 1
    0          | 1         | true              || 1
    1          | 1         | false             || 2
    1          | 1         | true              || 2
    0          | 2         | true              || 2
    1          | 2         | true              || 3
  }

  def "should work properly on field"() {
    expect:
    runner.runSpec """
${buildContainer(contained, containerWithList)}
${'@Foo\n' * individual}
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
    individual | contained | containerWithList || expected
    0          | -1        | false             || 0
    1          | -1        | false             || 1
    2          | -1        | false             || 2
    0          | 0         | false             || 0
    0          | 0         | true              || 0
    1          | 0         | false             || 1
    1          | 0         | true              || 1
    0          | 1         | false             || 1
    0          | 1         | true              || 1
    1          | 1         | false             || 2
    1          | 1         | true              || 2
    0          | 2         | true              || 2
    1          | 2         | true              || 3
  }

  static buildContainer(contained, containerWithList) {
    contained < 0 ? '' :
      '@FooContainer(' +
        (containerWithList ? '[' : '') +
        (['@Foo'] * contained).join(', ') +
        (containerWithList ? ']' : '') +
        ')'
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
