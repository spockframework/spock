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
 * Wraps an existing bean in a Spock spy.
 * <p>
 * Inspired by Springs {@code @SpyBean}, but adapted to Spock semantics.
 * <p>
 * Example: <pre class="code">
 *   &#064;SpringBootTest // &#064;ContextConfiguration works also
 *   class ExampleTests extends Specification {
 *     &#064;SpyBean
 *     Service spiedOnService
 *   }
 *   </pre>
 * <p>
 * Note, that like {@code @SpyBean} this will modify your {@code ApplicationContext}
 * an thus will prevent its reuse for other specs.
 * <p>
 * Only works on fields defined inside a Specification.
 * The field <b>must</b> have the type of the bean to spy upon, {@code def} or {@code Object}
 * fields don't work and will cause an error.
 * Furthermore, there must be an existing bean to wrap, otherwise an error will be thrown.
 * Also, the field <b>must not</b> use an initializer (e.g., {@code Service spiedOnService = Spy()}).
 * <p>
 * Requires at least Spring 4.3.3 to work properly, in earlier versions it should be
 * ignored silently.
 *
 * @author Leonard Brünings
 * @since 1.2
 */
@Beta
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringSpy {

  /**
   * The name of the bean to register or replace. If not specified the name will either
   * be generated or, if the mock replaces an existing bean, the existing name will be
   * used.
   * @return the name of the bean
   */
  String name() default "";
}
