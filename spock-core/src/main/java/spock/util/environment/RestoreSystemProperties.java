/*
 * Copyright 2012 the original author or authors.
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

package spock.util.environment;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.RestoreSystemPropertiesExtension;

import java.lang.annotation.*;

/**
 * Saves system properties before the annotated feature method (including any setup and cleanup methods) gets run,
 * and restores them afterwards.
 *
 * <p>Applying this annotation to a spec class has the same effect as applying it to all its feature methods.
 *
 * <p><strong>Note:</strong> Temporarily changing the values of system properties is only safe when specs are
 * run in a single thread per JVM. Even though many execution environments do limit themselves to one thread
 * per JVM, keep in mind that Spock cannot enforce this.
 *
 * <p>Note: If this extension is applied, then it will use acquire a lock for
 * {@link org.spockframework.runtime.model.parallel.Resources#SYSTEM_PROPERTIES}
 *
 * @see spock.lang.ResourceLock
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(RestoreSystemPropertiesExtension.class)
public @interface RestoreSystemProperties {}
