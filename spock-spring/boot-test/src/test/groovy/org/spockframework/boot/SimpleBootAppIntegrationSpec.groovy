/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.boot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.ApplicationContext

import spock.lang.Specification

/**
 * Spock test for {@link org.spockframework.boot.SimpleBootApp}.
 * Adapted from https://github.com/spring-projects/spring-boot/blob/master/spring-boot-samples/
 * spring-boot-sample-simple/src/test/java/sample/simple/SpringTestSampleSimpleApplicationTests.java.
 *
 * @author Dave Syer
 * @author Peter Niederwieser
 */
@SpringApplicationConfiguration(classes = SimpleBootApp.class)
class SimpleBootAppIntegrationSpec extends Specification {
  @Autowired
  ApplicationContext context

  def "test context loads"() {
    expect:
    context != null
    context.containsBean("helloWorldService")
    context.containsBean("simpleBootApp")
    context.containsBean("scopedHelloWorldService")
  }
}
