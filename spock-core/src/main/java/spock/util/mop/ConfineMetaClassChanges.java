/*
 * Copyright 2010 the original author or authors.
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

package spock.util.mop;

import java.lang.annotation.*;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ConfineMetaClassChangesExtension;

/**
 * Confines any changes made to the meta classes of the specified classes to the
 * annotated scope. This is done by installing new meta classes when the scope is
 * entered, and restoring the previously installed meta classes when the scope is
 * left. Note that this only works reliably as long as spec execution is
 * single-threaded.
 *
 * <p>If a spec class is annotated, the meta classes are restored to as they
 * were before <tt>setupSpec()</tt> was executed, after <tt>cleanupSpec</tt>
 * was executed.
 *
 * <p>If a feature method is annotated, the meta classes are restored to as they
 * were after <tt>setup()</tt> was executed, before <tt>cleanup() is executed.
 * For a data-driven feature method, meta classes are restored after each iteration.
 *
 *
 * <p>Note: If this extension is applied, then it will use acquire a lock for
 * {@link org.spockframework.runtime.model.parallel.Resources#META_CLASS_REGISTRY}
 *
 * @see spock.lang.ResourceLock
 *
 * @author Luke Daley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ConfineMetaClassChangesExtension.class)
@Repeatable(ConfineMetaClassChanges.Container.class)
public @interface ConfineMetaClassChanges {
  /**
   * The classes whose meta class changes are to be confined.
   */
  Class<?>[] value();

  /**
   * @since 2.0
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    ConfineMetaClassChanges[] value();
  }
}
