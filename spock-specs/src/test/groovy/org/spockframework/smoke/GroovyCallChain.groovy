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

package org.spockframework.smoke

/**
 * Important: This file needs to be exact copy of JavaCallChain.java.
 *
 * @author Peter Niederwieser
 */
class GroovyCallChain {
  void a() {
    b();
  }

  private int b() {
    new Inner().inner()
    return 0;
  }

  static void c(String foo, String bar) {
    throw new CallChainException();
  }

  class Inner {
      def inner() {
        new StaticInner().staticInner()
      }
  }

  static class StaticInner {
      def staticInner() {
        c("foo", "bar")
      }
  }
}
