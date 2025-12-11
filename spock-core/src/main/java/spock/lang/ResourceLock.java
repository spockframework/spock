/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package spock.lang;

import java.lang.annotation.*;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ResourceLockExtension;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;

/**
 * Allows to control access to a shared resource.
 *
 * If applied on class-level, the lock will be on the class,
 * covering shared fields and {@code setupSpec}/{@code cleanupSpec}.
 * This will also cause all features to run on the same thread
 * as the Specification.
 *
 * @see Isolated
 * @see Execution
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ResourceLockExtension.class)
@Repeatable(ResourceLock.Container.class)
public @interface ResourceLock {
  /**
   * The key identifying the resource.
   *
   * @see org.spockframework.runtime.model.parallel.Resources for a list of standard resources
   * @return the key
   */
  String value();

  /**
   * Controls the access mode of the resource.
   * @return mode to use
   */
  ResourceAccessMode mode() default ResourceAccessMode.READ_WRITE;

  /**
   * The reason for this lock
   *
   * @since 2.4
   */
  String reason() default "";

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    ResourceLock[] value();
  }
}
