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

package org.spockframework.dsl;

import java.lang.annotation.*;

import org.spockframework.runtime.intercept.Directive;
import org.spockframework.runtime.intercept.FailsWithProcessor;

/**
 * States that a feature method fails with the given exception. Useful for
 * pinpointing erroneous behavior until it gets fixed. To specify an expected
 * exception, use exception conditions instead
 * (e.g. "when: foo; then: thrown(MyException)").
 * When applied to a class, applies to all feature methods that aren't already
 * annotated with @FailsWith.
 *
 * @author Peter Niederwieser
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Directive(FailsWithProcessor.class)
public @interface FailsWith {
  Class<? extends Throwable> value();
  String reason() default "unknown";
}
