/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.runtime

import org.spockframework.util.IThrowableFunction
import spock.lang.Isolated
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@Isolated
class RunContextSpec extends Specification {
  def "initial run context is named 'default'"() {
    expect:
    RunContext.get().name == "default"
  }

  @RestoreSystemProperties
  def "Spock user home of initial run context can be configured via system property or environment variable"() {
    def dir = new File("foo", "bar")
    System.setProperty("spock.user.home", dir.path)

    expect:
    RunContext.createBottomContext().spockUserHome == dir
  }

  def "child context has its own name and Spock user home"() {
    def dir = new File("new", "home")

    expect:
    RunContext.withNewContext("new name", dir, null, [], [], false, {
      def context = RunContext.get()
      assert context.name == "new name"
      assert context.spockUserHome == dir
      true
    } as IThrowableFunction)

    and:
    RunContext.get().name == "default"
    RunContext.get().spockUserHome != dir
  }
}
