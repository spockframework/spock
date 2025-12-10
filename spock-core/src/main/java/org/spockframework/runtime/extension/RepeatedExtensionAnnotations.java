/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.runtime.extension;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;

/**
 * This annotation carries the information which repeatable annotations are actually repeated from the
 * AST transformation phase to the runtime phase. As repeated annotations are wrapped inside a container
 * annotation and even can be wrapped in the container but also be once applied directly,
 * you cannot simply use {@link AnnotatedElement#getAnnotations()} but also have to check each annotation
 * whether it is a container annotation for a repeatable extension annotation and then use its {@code value}
 * attribute. To save doing this effort at runtime using reflection, it is done during AST transformation,
 * and the result carried over to runtime phase using this annotation.
 *
 * <p>This annotation is just for internal usage and should not be applied manually.
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface RepeatedExtensionAnnotations {
  String VALUE = "value";

  Class<? extends Annotation>[] value();
}
