/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.StepwiseExtension;

import java.lang.annotation.*;

/**
 * <b>When applied to a spec,</b> the annotation indicates that the spec's feature methods should be run sequentially
 * in their declared order (even in the presence of a parallel spec runner), always starting from the first method.
 * If a method fails, the remaining methods will be skipped. Feature methods declared in super- and subspecs are not
 * affected.
 *
 * <p><b>When applied to a feature method,</b> the annotation analogically indicates that the feature's iterations
 * should be run sequentially in their declared order. If an iteration fails, the remaining iterations will be skipped.
 *
 * <p><tt>&#64;Stepwise</tt> is useful for specs with (logical) dependencies between methods. In particular, it helps to
 * avoid consecutive errors after a method has failed, which makes it easier to understand what really went wrong.
 * Analogically, it helps to avoid consecutive errors in subsequent iterations, when it is clear that the reason for one
 * iteration failure usually also causes subsequent iterations to fail, such as the availability of an external resource
 * in an integration test.
 *
 * <p><b><i>Please try to use this annotation as infrequently as possible</i></b> by refactoring to avoid dependencies
 * between feature methods or iterations whenever possible. Otherwise, you will lose opportunities to get meaningful
 * test results and good coverage.
 *
 * <p>Please note:
 * <ul>
 *   <li>Applying this annotation on a specification will activate
 *   {@link org.spockframework.runtime.model.parallel.ExecutionMode#SAME_THREAD ExecutionMode.SAME_THREAD} for the whole
 *   spec, all of its feature methods and their iterations (if any). If it is applied to a data-driven feature method
 *   only, it will set the same flag only per annotated method.
 *  </li>
 *  <li>
 *    <tt>&#64;Stepwise</tt> can be applied to methods since Spock 2.2. Therefore, for backward compatibility applying
 *    the annotation on spec level will <i>not</i> automatically skip subsequent feature method iterations upon failure
 *    in a previous iteration. If you want that in addition to (or instead of) step-wise spec mode, you do have to
 *    annotate each individual feature method you wish to have that capability. This also conforms to the principle that
 *    if you want to skip tests under whatever conditions, you ought to document your intent explicitly.
 *  </li>
 * </ul>
 *
 * @author Peter Niederwieser
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(StepwiseExtension.class)
public @interface Stepwise {}
