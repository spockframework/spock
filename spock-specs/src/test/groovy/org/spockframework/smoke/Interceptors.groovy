/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter
import spock.lang.ResourceLock

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class Interceptors extends EmbeddedSpecification {
  @Snapshot
  Snapshotter snapshotter

  def setup() {
    runner.addClassImport(LifecycleTest)
    LifecycleRecorderAndContextTesterExtension.lifecycleOutline = ''
  }

  @ResourceLock("LifecycleRecorderAndContextTesterExtension.lifecycleOutline")
  def "interceptors are called in the correct order and with the correct context information"() {
    when:
    runner.runWithImports """
abstract class SuperSpec extends Specification {
  @Shared superShared = 0

  def superInstance = 0

  def setupSpec() {
  }

  def setup() {
  }

  def superFeature1() {
    expect: true
  }

  def superFeature2() {
    expect: true
  }

  def superDataFeature1() {
    expect: x
    where:
    x << [true, true]
  }

  def superDataFeature2() {
    expect: x
    where:
    x << [true, true]
  }

  def cleanup() {
  }

  def cleanupSpec() {
  }
}

@LifecycleTest
class SubSpec extends SuperSpec {
  @Shared subShared = 0

  def subInstance = 0

  def setupSpec() {
  }

  def setup() {
  }

  def subFeature1() {
    expect: true
  }

  def subFeature2() {
    expect: true
  }

  def subDataFeature1() {
    expect: x
    where:
    x << [true, true]
  }

  def subDataFeature2() {
    expect: x
    where:
    x << [true, true]
  }

  def cleanup() {
  }

  def cleanupSpec() {
  }
}
"""

    then:
    snapshotter.assertThat(LifecycleRecorderAndContextTesterExtension.lifecycleOutline).matchesSnapshot()
  }

  @ResourceLock("LifecycleRecorderAndContextTesterExtension.lifecycleOutline")
  def "non-method interceptors are called even without the respective method"() {
    when:
    runner.runWithImports """
@LifecycleTest
class FooSpec extends Specification {
  def foo() {
    expect: true
  }

  def bar() {
    expect: x
    where:
    x << [true, true]
  }
}
"""

    then:
    snapshotter.assertThat(LifecycleRecorderAndContextTesterExtension.lifecycleOutline).matchesSnapshot()
  }

  static class LifecycleRecorderAndContextTesterExtension implements IAnnotationDrivenExtension<LifecycleTest> {
    static lifecycleOutline = ''
    def indent = ''

    @Override
    void visitSpecAnnotation(LifecycleTest annotation, SpecInfo specInfo) {
      specInfo.addSharedInitializerInterceptor {
        assertSpecContext(it)
        proceed(it, 'shared initializer', "$it.spec.name")
      }
      specInfo.allSharedInitializerMethods*.addInterceptor {
        assertSpecMethodContext(it)
        proceed(it, 'shared initializer method', "$it.spec.name.$it.method.name()")
      }
      specInfo.addInterceptor {
        assertSpecContext(it)
        proceed(it, 'specification', "$it.spec.name")
      }
      specInfo.addSetupSpecInterceptor {
        assertSpecContext(it)
        proceed(it, 'setup spec', "$it.spec.name")
      }
      specInfo.allSetupSpecMethods*.addInterceptor {
        assertSpecMethodContext(it)
        proceed(it, 'setup spec method', "$it.spec.name.$it.method.name()")
      }
      specInfo.allFeatures*.addInterceptor {
        assertFeatureContext(it)
        proceed(it, 'feature', "$it.spec.name.$it.feature.name")
      }
      specInfo.addInitializerInterceptor {
        assertIterationContext(it)
        proceed(it, 'initializer', "$it.spec.name.$it.feature.name")
      }
      specInfo.allInitializerMethods*.addInterceptor {
        assertIterationMethodContext(it)
        proceed(it, 'initializer method', "$it.spec.name.$it.feature.name.$it.method.name()")
      }
      specInfo.allFeatures*.addIterationInterceptor {
        assertIterationContext(it)
        proceed(it, 'iteration', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.addSetupInterceptor {
        assertIterationContext(it)
        proceed(it, 'setup', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.allSetupMethods*.addInterceptor {
        assertIterationMethodContext(it)
        proceed(it, 'setup method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.allFeatures*.featureMethod*.addInterceptor {
        assertIterationMethodContext(it)
        proceed(it, 'feature method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.addCleanupInterceptor {
        assertIterationContext(it)
        proceed(it, 'cleanup', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.allCleanupMethods*.addInterceptor {
        assertIterationMethodContext(it)
        proceed(it, 'cleanup method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.addCleanupSpecInterceptor {
        assertSpecContext(it)
        proceed(it, 'cleanup spec', "$it.spec.name")
      }
      specInfo.allCleanupSpecMethods*.addInterceptor {
        assertSpecMethodContext(it)
        proceed(it, 'cleanup spec method', "$it.spec.name.$it.method.name()")
      }
      specInfo.allFixtureMethods*.addInterceptor {
        it.with {
          def specFixture = method.name.endsWith('Spec')
          if (specFixture) {
            assertSpecMethodContext(it)
          } else {
            assertIterationMethodContext(it)
          }
        }
        proceed(it, 'fixture method', "$it.spec.name${it.feature?.name?.with { name -> ".$name[#$it.iteration.iterationIndex]" } ?: ''}.$it.method.name()")
      }
    }

    static assertSpecContext(IMethodInvocation invocation) {
      invocation.with {
        assert spec
        assert !feature
        assert !iteration
        assert instance == sharedInstance
        assert target != instance
      }
    }

    static assertSpecMethodContext(IMethodInvocation invocation) {
      invocation.with {
        assert spec
        assert !feature
        assert !iteration
        assert instance == sharedInstance
        assert target == instance
      }
    }

    static assertFeatureContext(IMethodInvocation invocation) {
      invocation.with {
        assert spec
        assert feature
        assert !iteration
        assert instance == sharedInstance
        assert target != instance
      }
    }

    static assertIterationContext(IMethodInvocation invocation) {
      invocation.with {
        assert spec
        assert feature
        assert iteration
        assert instance != sharedInstance
        assert target != instance
      }
    }

    static assertIterationMethodContext(IMethodInvocation invocation) {
      invocation.with {
        assert spec
        assert feature
        assert iteration
        assert instance != sharedInstance
        assert target == instance
      }
    }

    void proceed(IMethodInvocation invocation, String type, String name) {
      lifecycleOutline += "$indent$type start ($name)\n"
      indent += '  '
      invocation.proceed()
      indent -= '  '
      lifecycleOutline += "$indent$type end ($name)\n"
    }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(Interceptors.LifecycleRecorderAndContextTesterExtension)
@interface LifecycleTest {
}