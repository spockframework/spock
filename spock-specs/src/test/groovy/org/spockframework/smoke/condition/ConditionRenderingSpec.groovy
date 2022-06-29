/*
 * Copyright 2008 the original author or authors.
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

package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.Condition
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * Convenience base class for specs that describe rendering of conditions.
 *
 * @author Peter Niederwieser
 */
abstract class ConditionRenderingSpec extends EmbeddedSpecification {
  void isRendered(String expectedRendering, Condition condition) {
    def rendering = condition.rendering.trim()
    expectedRendering = expectedRendering.trim()
    assert rendering == expectedRendering
  }

  void isRendered(String expectedRendering, Closure condition) {
    try {
      condition()
    } catch (ConditionNotSatisfiedError e) {
      isRendered(expectedRendering, e.condition)
      return
    }

    assert false, "condition should have failed but didn't"
  }

  void renderedConditionContains(Closure condition, String... contents) {
    try {
      condition()
    } catch (ConditionNotSatisfiedError e) {
      String rendering = e.condition.rendering.trim()
      contents.each { assert rendering.contains(it)}
      return
    }

    assert false, "condition should have failed but didn't"
  }
}
