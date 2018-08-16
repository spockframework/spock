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

import groovy.lang.Closure;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.RetryExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.*;


/**
 * Retries the given feature if an exception occurs during execution.
 *
 * <p>Retries can be applied to feature methods and spec classes. Applying it to
 * a spec class has the same effect as applying it to each feature method that
 * isn't already annotated with {@code @Retry}.
 *
 * <p>{@code @Retry} annotations are inherited, i.e. if declared on a superclass
 * of a spec class, it will be applied to all feature methods of both classes
 * unless the subclass declares its own annotation. If so, it is applied to all
 * feature methods declared in the subclass, but not to any feature methods
 * declared in the superclass. In particular, that means that retries are not
 * applied to inherited feature methods if the annotation is only present on the
 * subclass.
 *
 * @author Leonard Brünings
 * @since 1.2
 */
@Beta
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
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
   * Condition that is evaluated to decide whether the feature should be
   * retried.
   *
   * The configured closure is called with a delegate of type
   * {@link org.spockframework.runtime.extension.builtin.RetryConditionContext}
   * which provides access to the current exception and {@code Specification}
   * instance.
   *
   * The feature is retried if the exception class passes the type check and the
   * specified condition holds true. If no condition is specified, only the type
   * check is performed.
   *
   * @return predicate that must hold for the feature to be retried.
   */
  Class<? extends Closure> condition() default Closure.class;

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
   * Retry mode, controls what is retried.
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
    ITERATION,

    /**
     * Retry the the feature together with the setup and cleanup methods.
     */
    SETUP_FEATURE_CLEANUP
  }
}
