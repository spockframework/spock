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

package spock.lang;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TimeoutExtension;

/**
 * Indicates that the execution of a method should time out
 * after the given duration has elapsed. The default time unit is seconds.
 *
 * <p>Timeouts can be applied to feature methods, fixture methods, and spec classes.
 * When applied to a feature method, the timeout is per execution of one iteration,
 * and does not include time spent in fixture methods. When applied to a fixture
 * method, the timeout is per execution of the fixture method. Applying the timeout
 * to a spec class has the same effect as applying it to each feature (but not fixture)
 * method that isn't already annotated with {@code Timeout}.
 *
 * <p>Timed methods are invoked on the regular test framework thread. This can be
 * important for integration tests that hold thread-local state. When a method
 * times out, it will be interrupted repeatedly until it (hopefully) returns.
 * Methods that continue to ignore interruption may run forever.
 *
 * <p>When a timeout is reported to the user, the stack trace shown reflects the
 * execution stack of the test framework thread when the timeout was reached.
 *
 * @author Peter Niederwieser
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(TimeoutExtension.class)
public @interface Timeout {
  /**
   * Returns the duration after which the execution of the annotated feature or fixture
   * method times out.
   *
   * @return the duration after which the execution of the annotated feature or
   * fixture method times out
   */
  int value();

  /**
   * Returns the duration's time unit.
   *
   * @return the duration's time unit
   */
  TimeUnit unit() default TimeUnit.SECONDS;
}
