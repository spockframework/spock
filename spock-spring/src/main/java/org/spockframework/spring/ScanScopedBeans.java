/*
 * Copyright 2017 the original author or authors.
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
 * Indicates that Spock should scan scoped spring beans for mocks.
 *
 * NOTE: Spock must be able to access these beans before the actual tests starts.
 * If you are using {@code request} or {@code session} scoped beans and want to
 * test them, then you'll probably need to use the
 * {@link org.springframework.context.support.SimpleThreadScope} in conjunction
 * with {@link org.springframework.beans.factory.config.CustomScopeConfigurer}.
 *
 * {@code prototype} scoped mocks will not work since spring will create different
 * instances for each injection target.
 */
@Beta
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanScopedBeans {
  /**
   * List of scopes to scan.
   *
   * If empty all scopes are included (default).
   * @return scopes to scan.
   */
  String[] value() default {};
}
