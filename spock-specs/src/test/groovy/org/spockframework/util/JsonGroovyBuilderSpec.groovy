
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

package org.spockframework.util

import spock.lang.*

class JsonGroovyBuilderSpec extends Specification {
  def builder = new JsonGroovyBuilder()

  def "render some simple json"() {
    setup:
    def spec = builder.json {
      clazz ="org.spockframework.smoke.MyFirstSpec"
      duration ="00:00:15.42"
      scenarios {
        name ="my first scenario"
        duration ="00:00:01.67"
        steps {
          kind ="when"
        }
        steps {
          kind ="then"
        }
      }
      scenarios {
        name ="my second scenario"
        duration ="00:00:00.31"
        steps {
          kind ="when"
          desc ="the mouse leaves the hole"
        }
        steps {
          kind ="then"
          desc ="the cat smiles happily"
        }
        exceptions((1..1).collect {
          exceptions {
            clazz ="foo.Bar"
          }
        })
      }


    }

    println spec.toString()
  }
}
