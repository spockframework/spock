/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Indicates that a spec's feature methods should be run sequentially
 * in their declared order (even in the presence of a parallel spec runner),
 * always starting from the first method. If a method fails, the remaining
 * methods will be skipped. Feature methods declared in super- and subspecs
 * are not affected.
 *
 * <p><tt>&#64;Stepwise</tt> is useful for specs with
 * (logical) dependencies between methods. In particular, it helps to avoid
 * consecutive errors after a method has failed, which makes it easier to
 * understand what really went wrong.
 *
 * <p>Note: If this extension is applied on the Specification, then it will use
 * {@link org.spockframework.runtime.model.parallel.ExecutionMode#SAME_THREAD}
 * for the whole Spec.
 *
 * @author Peter Niederwieser
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(StepwiseExtension.class)
public @interface Stepwise {}
