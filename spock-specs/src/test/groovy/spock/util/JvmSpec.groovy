/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spock.util

import spock.lang.Isolated
import spock.lang.Specification
import spock.util.environment.*

@Isolated
@RestoreSystemProperties
class JvmSpec extends Specification {
  def "can check for Java 8 version (old scheme)"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    assert jvm.java8 == (minor == 8)

    where:
    minor << (7..8)
  }

  def "can check for Java 8 version (old scheme) (given major version)"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    assert jvm.isJavaVersion(minor) == (minor == 8)

    where:
    minor << (7..8)
  }

  def "can check for Java 8 version compatibility (old scheme)"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    assert jvm."java8Compatible" == (minor >= 8)

    where:
    minor << (7..8)
  }

  def "can check for Java 8 version compatibility (old scheme) (given major version)"() {
    System.setProperty("java.specification.version", "1.$minor")
    def jvm = Jvm.current

    expect:
    assert jvm.isJavaVersionCompatible(minor) == (minor >= 8)

    where:
    minor << (7..8)
  }

  def "can check for Java version"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 8..23) {
      assert jvm."java$i" == (i == major)
    }

    where:
    major << (9..23)
  }

  def "can check for Java version compatibility"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 8..23) {
      assert jvm."java${i}Compatible" == (i <= major)
    }

    where:
    major << (9..23)
  }

  def "can check for Java version (given major version)"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 8..23) {
      assert jvm.isJavaVersion(i) == (i == major)
    }

    where:
    major << (9..23)
  }

  def "can check for Java version compatibility (given major version)"() {
    System.setProperty("java.specification.version", "$major")
    def jvm = Jvm.current

    expect:
    for (i in 8..23) {
      assert jvm.isJavaVersionCompatible(i) == (i <= major)
    }

    where:
    major << (9..23)
  }
}
