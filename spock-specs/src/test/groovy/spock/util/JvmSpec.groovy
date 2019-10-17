/*
 * Copyright 2012 the original author or authors.
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
package spock.util

import spock.lang.Specification
import spock.util.environment.*

@RestoreSystemProperties
class JvmSpec extends Specification {
  def "can check for Java version"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    for (i in 5..8) {
      assert jvm."java$i" == (i == minor)
    }

    where:
    minor << (5..8)
  }

  def "can check for Java version compatibility"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    for (i in 5..8) {
      assert jvm."java${i}Compatible" == (i <= minor)
    }

    where:
    minor << (5..8)
  }

  def "can check for Java version (new scheme)"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 5..11) {
      assert jvm."java$i" == (i == major)
    }

    where:
    major << (9..11)
  }

  def "can check for Java version compatibility (new scheme)"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 5..11) {
      assert jvm."java${i}Compatible" == (i <= major)
    }

    where:
    major << (9..11)
  }
}
