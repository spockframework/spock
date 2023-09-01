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

package org.spockframework.mock.runtime

import groovy.transform.Canonical
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import java.util.function.Function

class ByteBuddyMockFactoryConcurrentSpec extends Specification {
  private static final String IfA = "IfA"
  private static final String IfB = "IfB"
  private static final String IfC = "IfC"
  @Shared
  final IProxyBasedMockInterceptor interceptor = Stub()

  def "ensure lockMask bit patterns"() {
    expect:
    1 << Integer.bitCount(ByteBuddyMockFactory.CACHE_LOCK_MASK) - 1 == Integer.highestOneBit(ByteBuddyMockFactory.CACHE_LOCK_MASK)
  }

  // Just to be save to abort, normally the tests run in 2 secs.
  @Timeout(120)
  def "cacheLockingStressTest #test"() {
    given:
    int iterations = 5_000
    def tempClassLoader = new ByteBuddyTestClassLoader()
    MockFeatures featA = toMockFeatures(mockSpecA, tempClassLoader)
    MockFeatures featB = toMockFeatures(mockSpecB, tempClassLoader)
    ByteBuddyMockFactory mockFactory = new ByteBuddyMockFactory()

    Phaser phaser = new Phaser(4)
    Function<Runnable, CompletableFuture<Void>> runCode = { Runnable code ->
      CompletableFuture.runAsync {
        phaser.arriveAndAwaitAdvance()
        try {
          for (int i = 0; i < iterations; i++) {
            code.run()
          }
        } finally {
          phaser.arrive()
        }
      }
    }
    when:
    def mockFeatAFuture = runCode.apply {
      Class<?> mockClass = mockClass(mockFactory, tempClassLoader, featA)
      assertValidMockClass(featA, mockClass, tempClassLoader)
    }

    def mockFeatBFuture = runCode.apply {
      Class<?> mockClass = mockClass(mockFactory, tempClassLoader, featB)
      assertValidMockClass(featB, mockClass, tempClassLoader)
    }

    def cacheFuture = runCode.apply { mockFactory.CACHE.clear() }

    phaser.arriveAndAwaitAdvance()
    // Wait for test to end
    int phase = phaser.arrive()
    try {
      phaser.awaitAdvanceInterruptibly(phase, 30, TimeUnit.SECONDS)
    } finally {
      // Collect exceptions from the futures, to make issues visible.
      mockFeatAFuture.getNow(null)
      mockFeatBFuture.getNow(null)
      cacheFuture.getNow(null)
    }
    then:
    noExceptionThrown()

    where:
    test                                      | mockSpecA          | mockSpecB
    "same hashcode different mockType"        | mockSpec(IfA, IfB) | mockSpec(IfB, IfA)
    "same hashcode same mockType"             | mockSpec(IfA)      | mockSpec(IfA)
    "different hashcode different interfaces" | mockSpec(IfA, IfB) | mockSpec(IfB, IfC)
    "unrelated classes"                       | mockSpec(IfA)      | mockSpec(IfB)
  }

  private Class<?> mockClass(ByteBuddyMockFactory mockFactory, ClassLoader cl, MockFeatures feature) {
    def settings = MockCreationSettings.settings(feature.mockType, feature.interfaces, interceptor, cl, false)
    return mockFactory.createMock(settings).getClass()
  }

  private static MockSpec mockSpec(String mockedType, String... interfaces) {
    return new MockSpec(mockedType, interfaces as List)
  }

  private void assertValidMockClass(MockFeatures mockFeature, Class<?> mockClass, ClassLoader classLoader) {
    assert mockClass.classLoader == classLoader
    assert mockFeature.mockType.isAssignableFrom(mockClass)
    mockFeature.interfaces.each {
      assert it.isAssignableFrom(mockClass)
    }
  }

  MockFeatures toMockFeatures(MockSpec mockFeaturesString, ByteBuddyTestClassLoader classLoader) {
    def mockType = classLoader.defineInterface(mockFeaturesString.mockType)
    def interfaces = mockFeaturesString.interfaces.collect { classLoader.defineInterface(it) }
    return new MockFeatures(mockType, interfaces)
  }

  /**
   * Class holding the loaded classes to mock.
   */
  @Canonical
  private static class MockFeatures {
    final Class<?> mockType
    final List<Class<?>> interfaces
  }

  /**
   * Class holding the class names to mock.
   * Which will be converted into a {@link MockFeatures} during test.
   */
  @Canonical
  private static class MockSpec {
    final String mockType
    final List<String> interfaces
  }
}
