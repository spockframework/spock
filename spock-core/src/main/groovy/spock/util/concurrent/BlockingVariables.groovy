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

package spock.util.concurrent

import java.util.concurrent.TimeUnit

class BlockingVariables {
  private final BlockingVariablesImpl impl

  BlockingVariables() {
    impl = new BlockingVariablesImpl()
  }

  BlockingVariables(int timeout, TimeUnit unit) {
    impl = new BlockingVariablesImpl(timeout, unit)
  }

  void setProperty(String name, Object value) {
    impl.put(name, value)
  }

  Object getProperty(String name) {
    impl.get(name)
  }
}