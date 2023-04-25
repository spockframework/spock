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
class SuperSpec extends Specification {
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
    noExceptionThrown()
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
    noExceptionThrown()
    snapshotter.assertThat(LifecycleRecorderAndContextTesterExtension.lifecycleOutline).matchesSnapshot()
  }

  static class LifecycleRecorderAndContextTesterExtension implements IAnnotationDrivenExtension<LifecycleTest> {
    static lifecycleOutline = ''
    def indent = ''

    @Override
    void visitSpecAnnotation(LifecycleTest annotation, SpecInfo specInfo) {
      specInfo.addSharedInitializerInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'shared initializer', "$it.spec.name")
      }
      specInfo.allSharedInitializerMethods*.addInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'shared initializer method', "$it.spec.name.$it.method.name()")
      }
      specInfo.addInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'specification', "$it.spec.name")
      }
      specInfo.addSetupSpecInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'setup spec', "$it.spec.name")
      }
      specInfo.allSetupSpecMethods*.addInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'setup spec method', "$it.spec.name.$it.method.name()")
      }
      specInfo.allFeatures*.addInterceptor {
        it.with {
          assert spec
          assert feature
          assert !iteration
          assert instance == sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'feature', "$it.spec.name.$it.feature.name")
      }
      specInfo.addInitializerInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'initializer', "$it.spec.name.$it.feature.name")
      }
      specInfo.allInitializerMethods*.addInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'initializer method', "$it.spec.name.$it.feature.name.$it.method.name()")
      }
      specInfo.allFeatures*.addIterationInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'iteration', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.addSetupInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'setup', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.allSetupMethods*.addInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'setup method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.allFeatures*.featureMethod*.addInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'feature method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.addCleanupInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'cleanup', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex]")
      }
      specInfo.allCleanupMethods*.addInterceptor {
        it.with {
          assert spec
          assert feature
          assert iteration
          assert instance != sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            assert currentFeature
            assert currentIteration
          }
        }
        proceed(it, 'cleanup method', "$it.spec.name.$it.feature.name[#$it.iteration.iterationIndex].$it.method.name()")
      }
      specInfo.addCleanupSpecInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target != instance
          assert method
          assert !method.reflection
          assert !method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'cleanup spec', "$it.spec.name")
      }
      specInfo.allCleanupSpecMethods*.addInterceptor {
        it.with {
          assert spec
          assert !feature
          assert !iteration
          assert instance == sharedInstance
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            try {
              currentFeature
              assert false: 'currentFeature should not be set'
            } catch (IllegalStateException ise) {
            }
            try {
              currentIteration
              assert false: 'currentIteration should not be set'
            } catch (IllegalStateException ise) {
            }
          }
        }
        proceed(it, 'cleanup spec method', "$it.spec.name.$it.method.name()")
      }
      specInfo.allFixtureMethods*.addInterceptor {
        it.with {
          assert spec
          def specFixture = method.name.endsWith('Spec')
          if (specFixture) {
            assert !feature
            assert !iteration
            assert instance == sharedInstance
          } else {
            assert feature
            assert iteration
            assert instance != sharedInstance
          }
          assert target == instance
          assert method
          assert method.reflection
          assert method.name
          instance.specificationContext.with {
            assert currentSpec
            if (specFixture) {
              try {
                currentFeature
                assert false: 'currentFeature should not be set'
              } catch (IllegalStateException ise) {
              }
              try {
                currentIteration
                assert false: 'currentIteration should not be set'
              } catch (IllegalStateException ise) {
              }
            } else {
              assert currentFeature
              assert currentIteration
            }
          }
        }
        proceed(it, 'fixture method', "$it.spec.name${it.feature?.name?.with { name -> ".$name[#$it.iteration.iterationIndex]" } ?: ''}.$it.method.name()")
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
