/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException

class InvalidStubInteraction extends EmbeddedSpecification {
  def 'required interaction on field mock but not on stub'() {
    when:
    runner.runSpecBody("""
List listMock = Mock {
  0 * size()
}

List list = Stub()

def foo() { expect: !list.size() }
    """)

    then:
    notThrown(InvalidSpecException)
  }

  def 'required interaction on local mock but not on stub'() {
    when:
    List listMock = Mock {
      0 * size()
    }

    and:
    List list = Stub()

    and:
    list.size()

    then:
    notThrown(InvalidSpecException)
  }

  def 'required interaction on field stub that is called'() {
    when:
    runner.runSpecBody("""
List list = Stub {
  0 * size()
}

def foo() { expect: !list.size() }
    """)

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }

  def 'required interaction on local stub that is called'() {
    when:
    List list = Stub {
      0 * size()
    }

    and:
    list.size()

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }

  def 'required interaction on field stub that is not called but another method is'() {
    when:
    runner.runSpecBody("""
List list = Stub {
  0 * size()
}

def foo() { expect: list.isEmpty() }
    """)

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }

  def 'required interaction on local stub that is not called but another method is'() {
    when:
    List list = Stub {
      0 * size()
    }

    and:
    list.isEmpty()

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }

  def 'required interaction on field stub that is not called at all'() {
    when:
    runner.runSpecBody("""
List list = Stub {
  0 * size()
}

def foo() { expect: true }
    """)

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }

  def 'required interaction on local stub that is not called at all'() {
    when:
    List list = Stub {
      0 * size()
    }

    then:
    InvalidSpecException ise = thrown()
    ise.message.contains('turn the stub into a mock')
  }
}
