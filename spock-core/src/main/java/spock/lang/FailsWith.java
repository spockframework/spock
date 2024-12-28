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

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.FailsWithExtension;

/**
 * Indicates that a feature method is expected to fail with the given exception.
 * Useful for pinpointing bugs until they get fixed, or as a replacement for
 * exception conditions in certain corner cases where they cannot be used (like
 * specifying the behavior of exception conditions). In all other cases,
 * exception conditions are preferable.
 * <p>Applying this annotation to a specification has the same effect as applying it
 * to all feature methods that aren't already annotated with <tt>@FailsWith</tt>.
 *
 * @author Peter Niederwieser
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtensionAnnotation(FailsWithExtension.class)
public @interface FailsWith {
  /**
   * The expected exception type.
   *
   * @return the expected exception type
   */
  Class<? extends Throwable> value();

  /**
   * The reason for the failure.
   *
   * @return the reason for the failure
   */
  String reason() default "unknown";

  /**
   * The expected message of the exception.
   * <p>
   * If the value is empty, the message is not checked.
   *
   * @since 2.4
   */
  String expectedMessage() default "";
}
