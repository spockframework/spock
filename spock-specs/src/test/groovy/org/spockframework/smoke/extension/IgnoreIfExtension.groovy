/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import spock.lang.*

class IgnoreIfExtension extends Specification {
  @IgnoreIf({ 1 < 2 })
  def "basic usage"() {
    expect: false
  }

  @IgnoreIf({ os.windows || os.linux || os.macOs || os.solaris || os.other })
  def "provides OS information"() {
    expect: false
  }

  @IgnoreIf({ jvm.java5 || jvm.java6 || jvm.java7 || jvm.java8 || jvm.java9 })
  def "provides JVM information"() {
    expect: false
  }

  @IgnoreIf({ !env.containsKey("FOOBARBAZ") })
  def "provides access to environment variables"() {
    expect: false
  }

  @IgnoreIf({ !sys.contains("java.version") })
  def "provides access to system properties"() {
    expect: false
  }

  @Issue("https://github.com/spockframework/spock/issues/535")
  @Requires({ false })
  @IgnoreIf({ false })
  def "allows determinate use of multiple filters" () {
    expect: false
  }
}


