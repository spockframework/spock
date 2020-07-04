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

/**
 * The tests in this class use a repeatable annotation {@code @Foo} with a
 * container annotation {@code @FooContainer} and an extension {@code @FooExtension}
 * that sets static fields in the ran specification to count how often the extension
 * was triggered by the annotations.
 *
 * <p>The tests are all data-driven features that test a different scenario
 * (applied to spec, feature, fixture, field) with different combinations of
 * individual standalone {@code @Foo} annotations and ones contained within the
 * container annotation.
 *
 * <p>The data variables meanings are:
 * <dl>
 *   <dt>individual</dt>
 *   <dd>How many individual standalone annotations to generate</dd>
 *
 *   <dt>contained</dt>
 *   <dd>How many contained annotations to generate (-1 means omit the container completely)</dd>
 *
 *   <dt>containerWithList</dt>
 *   <dd>Whether to use list syntax for the container value or not</dd>
 *
 *   <dt>expected</dt>
 *   <dd>How many extension invocations are expected</dd>
 * </dl>
 */
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

  def "repeated annotations work properly on feature"() {
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

  def "repeated annotations work properly on fixture"() {
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

  def "repeated annotations work properly on field"() {
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

  /**
   * Builds the annotation container {@code @FooContainer} with the given amount of
   * {@code @Foo} annotations, either in a list or plain. An amount of {@code -1} means
   * no container annotation at all. With list {@code false} only makes sense for counts
   * less than {@code 2}, otherwise invalid syntax is produced.
   *
   * <p><b>Examples:</b>
   * <table>
   *   <th><td> contained </td><td> containerWithList </td><td> returnValue                   </td></th>
   *   <tr><td> -1        </td><td> n/a               </td><td> ''                            </td></tr>
   *   <tr><td> 0         </td><td> false             </td><td> '@FooContainer()'             </td></tr>
   *   <tr><td> 0         </td><td> true              </td><td> '@FooContainer([])'           </td></tr>
   *   <tr><td> 1         </td><td> false             </td><td> '@FooContainer(@Foo)'         </td></tr>
   *   <tr><td> 1         </td><td> true              </td><td> '@FooContainer([@Foo])'       </td></tr>
   *   <tr><td> 2         </td><td> true              </td><td> '@FooContainer([@Foo, @Foo])' </td></tr>
   * </table>
   *
   * @param contained the amount of {@code @Foo} annotations to include
   * @param containerWithList whether to wrap the annotations in a {@code List} or have a plain value
   * @return the built container annotation
   */
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
