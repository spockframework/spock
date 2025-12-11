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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers mock/stub/spy as a spring bean in the test context.
 * <p>
 * Inspired by Springs {@code @MockBean}, but adapted to Spock semantics.
 * <p>
 * Example: <pre class="code">
 *   &#064;SpringBootTest // &#064;ContextConfiguration works also
 *   class ExampleTests extends Specification {
 *     &#064;SpringBean
 *     Service mockService = Mock()
 *
 *     &#064;SpringBean
 *     Service stubService = Stub() {
 *       stubCall() >> "default interaction can be defined to"
 *     }
 *
 *     &#064;SpringBean
 *     Service spyService = Spy()
 *   }
 *   </pre>
 * <p>
 * Note, that like {@code @MockBean} this will modify your {@code ApplicationContext}
 * an thus will prevent its reuse for other specs.
 * <p>
 * The created mocks belong to the specification and will not work outside of it.
 * <p>
 * Only works on fields defined inside a Specification.
 * The field <b>must</b> have the type of the mock, {@code def} or {@code Object}
 * fields don't work and will cause an error.
 * Also, the field <b>must</b> use an initializer (e.g., {@code Service mockService = Mock()})
 * assigning a value in {@code setup} doesn't work and will cause an error.
 * <p>
 * Requires at least Spring 4.3.3 to work properly, in earlier versions it should be
 * ignored silently.
 *
 * @author Leonard Brünings
 * @since 1.2
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringBean {

  /**
   * The name of the bean to register or replace. If not specified the name will either
   * be generated or, if the mock replaces an existing bean, the existing name will be
   * used.
   * @return the name of the bean
   */
  String name() default "";

  /**
   * List of aliases for this bean, use this if you want to have additional names for
   * your bean, but want to keep the logic of using the name of the replaced bean.
   *
   * Note the name of the annotated field is automatically used as alias.
   *
   * @return the alias names
   */
  String[] aliases() default {};
}
