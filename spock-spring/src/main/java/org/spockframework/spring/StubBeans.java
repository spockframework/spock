/*
 * Copyright 2018 the original author or authors.
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
package org.spockframework.spring;

import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Adds one or more plain Stubs to the ApplicationContext.
 *
 * Use this if you just need to satisfy some dependencies without
 * actually doing anything with these stubs.
 * <p>
 * If you need to control the stubs, e.g., configure return values
 * then use {@link SpringBean} instead.
 * <p>
 * Like {@link SpringBean} {@link StubBeans} also replaced existing BeanDefinitions,
 * so you can use it to remove real beans from an ApplicationContext.
 * <p>
 * {@link StubBeans} can be replaced by {@link SpringBean}, this can be useful
 * if you need to replace some {@link StubBeans} defined in a parent class.
 *
 * @author Leonard Brünings
 * @since 1.2
 */
@Beta
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StubBeans {
  /**
   * List of classes to stub
   * @return classes to stub
   */
  Class<?>[] value();

  /**
   * Controls if parent StubBean declarations should be inherited.
   *
   * @return true if inherit false if only this one
   */
  boolean inherit() default true;
}
