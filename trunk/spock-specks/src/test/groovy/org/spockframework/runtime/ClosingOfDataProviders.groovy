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

package org.spockframework.runtime

import org.junit.runner.RunWith
import org.spockframework.dsl.*
import static org.spockframework.dsl.Predef.*

@Speck
@RunWith(Sputnik)
class ClosingOfDataProviders {
  def runner = new SpeckInfoParameterizedRunner(null, null)

  def "closeOneProviderWhichPotentiallyThrowsException"() {
    MyCloseable provider = Mock()

    when: runner.closeDataProviders([provider] as Object[])
    then: 1 * provider.close() >> { action() }
    where:
      action << [{}, { throw new Exception() }]
  }

  def "closeMultipleProvidersWhichPotentiallyThrowException"() {
    MyCloseable provider1 = Mock()
    java.io.Closeable provider2 = Mock()

    when:
      runner.closeDataProviders([provider1, provider2] as Object[])
    then:
      1 * provider1.close() >> { action() }
      1 * provider2.close()
    where:
      action << [{}, { throw new Exception() }]
  }
}

interface MyCloseable {
  void close()
}