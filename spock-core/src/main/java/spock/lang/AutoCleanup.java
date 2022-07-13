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

import java.lang.annotation.*;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.AutoCleanupExtension;

/**
 * Automatically cleans up the object stored in the annotated field or property
 * at the end of its life time. More precisely, auto-cleanup of an object has
 * the same effect as, and serves as a convenient replacement for, calling the
 * object's <tt>close</tt> method at the end of the spec's <tt>cleanup</tt> method.
 * <tt>&#64;Shared</tt> objects are cleaned up at the end of the spec's
 * <tt>cleanupSpec</tt> method.

 * <h3>Customizing how cleanup is performed</h3>
 * By default, an object is cleaned up by invoking its parameterless close()
 * method (which is assumed to exist); visibility and return type of this method
 * are irrelevant. If some other method should be called instead, override the
 * annotation's <tt>value</tt> attribute:
 *
 * <pre>
 * &#64;AutoCleanup("dispose") // invoke the object's "dispose" method
 * </pre>
 *
 * <h3>Cleaning up multiple objects</h3>
 * If multiple fields or properties are annotated with <tt>&#64;AutoCleanup</tt>,
 * their objects are cleaned up sequentially in reverse field/property declaration
 * order, starting from the most derived class and walking up the inheritance chain.
 *
 * <h3>Handling of exceptions during cleanup</h3>
 * If a cleanup operation fails with an exception, the exception is reported
 * (just as if it had occurred in a <tt>cleanup</tt> or <tt>cleanupSpec</tt>
 * method) and cleanup proceeds with the next annotated object. To prevent
 * cleanup exceptions from being reported, override the annotation's <tt>quiet</tt>
 * attribute:
 *
 * <pre>
 * &#64;AutoCleanup(quiet = true) // don't report exceptions
 * </pre>
 *
 * @author Peter Niederwieser
 */
@ExtensionAnnotation(AutoCleanupExtension.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCleanup {
  String value() default "close";
  boolean quiet() default false;
}
