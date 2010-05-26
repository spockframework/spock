/*
 * Copyright 2010 the original author or authors.
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

import spock.lang.*

/**
 * The expected order of interactions can now be specified by using multiple
 * then-blocks. Interactions in a later then-block must take place after
 * interactions in an earlier then-block. The order of interactions within
 * the same then-block is unspecified.
 *
 * @since 0.4
 */
class OrderedInteractions extends Specification {
  def "collaborators must be invoked in order"() {
    def coll1 = Mock(Collaborator)
    def coll2 = Mock(Collaborator)

    when:
    // try to reverse the order of these invocations and see what happens
    coll1.collaborate()
    coll2.collaborate()

    then:
    1 * coll1.collaborate()

    then:
    1 * coll2.collaborate()
  }
}

interface Collaborator {
  def collaborate()
}
