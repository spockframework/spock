/*
 * Copyright 2010 the original author or authors.
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

package spock.util.mop;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.UseExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Activates one or more Groovy categories while the annotated spec method
 * or class executes. In other words, <tt>&#64;Use(SomeCategory)</tt>
 * has the same effect as wrapping the execution of the annotated method or
 * class with <tt>use(SomeCategory) { ... }</tt>.
 *
 * <p>Basic example:
 *
 * <pre>
 * class ListExtensions {
 *   static avg(List list) { list.sum() / list.size() }
 * }
 *
 * class MySpec extends Specification {
 *   &#64;Use(ListExtensions)
 *   def "can use avg() method"() {
 *     expect:
 *     [1, 2, 3].avg() == 2
 *   }
 * }
 * </pre>
 *
 * <p>One use case for this feature is the stubbing of dynamic methods which
 * are usually provided by the runtime environment (e.g. Grails).
 *
 * <p>Note: <tt>&#64;Use</tt> has no effect when applied to a helper method.
 * However, when applied to a spec class it will also affect its helper methods.
 *
 * <p>Note: If this extension is applied on the Specification, then it will use
 * {@link org.spockframework.runtime.model.parallel.ExecutionMode#SAME_THREAD}
 * for the whole Spec.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(UseExtension.class)
@Repeatable(Use.Container.class)
public @interface Use {
  Class[] value();

  /**
   * @since 2.0
   */
  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    Use[] value();
  }
}
