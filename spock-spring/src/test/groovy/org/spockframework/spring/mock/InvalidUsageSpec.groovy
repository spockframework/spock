/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.spring.mock

import org.spockframework.spring.SpringExtensionException
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class InvalidUsageSpec extends Specification {
  EmbeddedSpecRunner runner = new EmbeddedSpecRunner()

  def "@SpringBean requires an initializer"() {
    when:
    runner.run('''
    import org.spockframework.spring.*
    import spock.lang.Specification

    import org.springframework.beans.factory.annotation.Autowired
    import org.springframework.test.context.ContextConfiguration

    @ContextConfiguration(classes = org.spockframework.spring.mock.DemoMockContext)
    class ASpec extends Specification {

      @SpringBean
      Service2 service2

      def test() {
        expect: true
      }
    }
    ''')

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Field 'ASpec.service2:10' needs to have an initializer, e.g. List l = Mock()"
  }


  def "@SpringBean requires a concrete type on field"() {
    when:
    runner.run('''
    import org.spockframework.spring.*
    import spock.lang.Specification

    import org.springframework.beans.factory.annotation.Autowired
    import org.springframework.test.context.ContextConfiguration

    @ContextConfiguration(classes = org.spockframework.spring.mock.DemoMockContext)
    class ASpec extends Specification {

      @SpringBean
      def service2 = Mock(Service2)

      def test() {
        expect: true
      }
    }
    ''')

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Field 'ASpec.service2:10' must use a concrete type, not def or Object."
  }

  def "@SpringSpy requires a concrete type on field"() {
    when:
    runner.run('''
    import org.spockframework.spring.*
    import spock.lang.Specification

    import org.springframework.beans.factory.annotation.Autowired
    import org.springframework.test.context.ContextConfiguration

    @ContextConfiguration(classes = org.spockframework.spring.mock.DemoMockContext)
    class ASpec extends Specification {

      @SpringSpy
      def service2

      def test() {
        expect: true
      }
    }
    ''')

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Field 'ASpec.service2:10' must use a concrete type, not def or Object."
  }

  def "@SpringSpy cannot have an initializer"() {
    when:
    runner.run('''
    import org.spockframework.spring.*
    import spock.lang.Specification

    import org.springframework.beans.factory.annotation.Autowired
    import org.springframework.test.context.ContextConfiguration

    @ContextConfiguration(classes = org.spockframework.spring.mock.DemoMockContext)
    class ASpec extends Specification {

      @SpringSpy
      Service2 service2 = Mock()

      def test() {
        expect: true
      }
    }
    ''')

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Field 'ASpec.service2:10' may not have an initializer."
  }
}
