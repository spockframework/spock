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

package spock.lang;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import org.spockframework.runtime.intercept.Directive;
import org.spockframework.runtime.intercept.TimeoutProcessor;

/**
 * Indicates that the execution of a feature or fixture method should time out
 * after the given duration has elapsed. The default time unit is seconds.
 * When applied to a feature method, the timeout is reset before each
 * iteration and does not include time spent in fixture methods.
 *
 * @author Peter Niederwieser
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Directive(TimeoutProcessor.class)
public @interface Timeout {
  /**
   * The duration after which the execution of the annotated feature or fixture
   * method times out.
   *
   * @return the duration after which the execution of the annotated feature or
   * fixture method times out
   */
  int value();

  /**
   * The duration's time unit
   *
   * @return the duration's time unit
   */
  TimeUnit unit() default TimeUnit.SECONDS;
}
