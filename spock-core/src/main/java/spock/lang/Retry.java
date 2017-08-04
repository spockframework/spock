/*
 * Copyright 2017 the original author or authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.RetryExtension;
import org.spockframework.util.Beta;


/**
 * @author Leonard Brünings
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
@ExtensionAnnotation(RetryExtension.class)
public @interface Retry {
  /**
   * Configures which types of Exceptions should be retried.
   *
   * Subclasses are included if their parent class is listed.
   *
   * @return array of Exception classes to retry.
   */
  Class<? extends Throwable>[] exceptions() default {Exception.class, AssertionError.class};

  /**
   * The number of retries.
   *
   * @return number of retries.
   */
  int count() default 3;

  /**
   * Delay between retries in millis, 0 to disable.
   *
   * @return number of millis to wait.
   */
  int delay() default 0;

  /**
   * Retry mode, only relevant for data driven features.
   *
   * @return retry mode
   */
  Mode mode() default Mode.ITERATION;

  enum Mode {
    /**
     * Retry the whole feature, if any iteration fails.
     */
    FEATURE,

    /**
     * Retry the iterations individually.
     */
    ITERATION
  }
}
