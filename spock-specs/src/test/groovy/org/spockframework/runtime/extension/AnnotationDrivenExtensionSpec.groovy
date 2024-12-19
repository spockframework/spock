/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.runtime.extension

import spock.config.ConfigurationObject

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.util.concurrent.atomic.AtomicInteger

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo

class AnnotationDrivenExtensionSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(Stateful)
    runner.addClassImport(Stateless)
    runner.configClasses << ExtensionCounter
  }

  def "stateless extensions get re-used and stateful extensions get fresh instances per spec"() {
    given:
    def stateful = new AtomicInteger()
    def stateless = new AtomicInteger()
    runner.configurationScript {
      extensionCounter {
        statefulCounter stateful
        statelessCounter stateless
      }
    }

    when:
    runner.runWithImports('''
    @Stateful
    @Stateless
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }
    }
    
    @Stateful
    @Stateless
    class BSpec extends Specification {
      def "b feature"() {
        expect: true
      }
    }
    
    @Stateful
    @Stateless
    class CSpec extends Specification {
      def "c feature"() {
        expect: true
      }
    }
    ''')

    then:
    stateful.get() == 3
    stateless.get() == 1
  }

  def "both stateful and stateless only have one instance per spec regardless of use"() {
    given:
    def stateful = new AtomicInteger()
    def stateless = new AtomicInteger()
    runner.configurationScript {
      extensionCounter {
        statefulCounter stateful
        statelessCounter stateless
      }
    }

    when:
    runner.runWithImports('''
    @Stateful
    @Stateless
    class ASpec extends Specification {
      
      @Stateful
      @Stateless
      int a
      
      @Stateful
      @Stateless
      int b
      
      @Stateful
      @Stateless
      def setupSpec() {
      }
      
      @Stateful
      @Stateless
      def setup() {
      }
   
      
      @Stateful
      @Stateless
      def "a feature"() {
        expect: true
      }
      @Stateful
      @Stateless
      def "b feature"() {
        expect: true
      }
    }
    
    ''')

    then:
    stateful.get() == 1
    stateless.get() == 1
  }
}


@Target([ElementType.TYPE, ElementType.METHOD, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(StatefulExtension)
@interface Stateful {
}

class StatefulExtension implements IAnnotationDrivenExtension<Stateful> {

  StatefulExtension(ExtensionCounter extensionCounter) {
    extensionCounter.statefulCounter.incrementAndGet()
  }

  @Override
  void visitSpecAnnotation(Stateful annotation, SpecInfo spec) {
  }

  @Override
  void visitFieldAnnotation(Stateful annotation, FieldInfo field) {
  }

  @Override
  void visitFeatureAnnotation(Stateful annotation, FeatureInfo feature) {
  }

  @Override
  void visitFixtureAnnotation(Stateful annotation, MethodInfo fixtureMethod) {
  }
}

@Target([ElementType.TYPE, ElementType.METHOD, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(StatelessExtension)
@interface Stateless {
}

class StatelessExtension implements IStatelessAnnotationDrivenExtension<Stateless> {

  StatelessExtension(ExtensionCounter extensionCounter) {
    extensionCounter.statelessCounter.incrementAndGet()
  }

  @Override
  void visitSpecAnnotation(Stateless annotation, SpecInfo spec) {
  }

  @Override
  void visitFieldAnnotation(Stateless annotation, FieldInfo field) {
  }

  @Override
  void visitFeatureAnnotation(Stateless annotation, FeatureInfo feature) {
  }

  @Override
  void visitFixtureAnnotation(Stateless annotation, MethodInfo fixtureMethod) {
  }
}

@ConfigurationObject("extensionCounter")
class ExtensionCounter {
  AtomicInteger statefulCounter
  AtomicInteger statelessCounter
}
