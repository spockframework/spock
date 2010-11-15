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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.UseExtension;

/**
 * Activates one or more Groovy categories while the annotated method or
 * specification executes. In other words, <tt>&#64;Use(MyCategory)</tt>
 * has the same effect as wrapping the execution of the annotated method or
 * specification with <tt>use(MyCategory) { ... }</tt>.
 *
 * <p>Basic example:
 *
 * <pre>
 * class MyCategory {
 *   // add avg() method on lists
 *   static avg(List list) { list.sum() / list.size() }
 * }
 *
 * class MySpec extends Specification {
 *   &#64;Use(MyCategory)
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
 * However, when applied to a specification it will also affect its helper methods.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(UseExtension.class)
public @interface Use {
  Class[] value();
}
