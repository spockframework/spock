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

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.IgnoreExtension;

/**
 * Indicates that a specification or feature method should not be run.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(IgnoreExtension.class)
public @interface Ignore {
  /**
   * The reason for ignoring this element.
   *
   * @return the string to use for the skip message
   */
  String value() default "";

  /**
   * Whether this annotation should be inherited by child classes.
   *
   * For historic reasons, this is false by default.
   * It has no special effect if used on a feature.
   *
   * @since 2.1
   * @return whether this annotation applies to child classes
   */
  boolean inherited() default false;
}
