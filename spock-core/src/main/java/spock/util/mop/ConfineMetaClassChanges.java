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

import java.lang.annotation.*;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ConfineMetaClassChangesExtension;

/**
 * Causes Spock to revert the meta class of the given classes to the state
 * they were in before execution of the construct annotated with @ConfineMetaClassChanges.
 * 
 * <p>If a spec class is annotated, the meta class(es) are reverted to as they were before
 * any methods were executed (including setupSpec()), after all methods are executed
 * (i.e. after cleanupSpec()).
 * 
 * <p>If a feature method is annotated, the meta class(es) are reverted to as they were before
 * the feature was executed, after the feature executes. For a data-driven feature method,
 * meta classes are reverted after each iteration.
 * 
 * @author Luke Daley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ConfineMetaClassChangesExtension.class)
public @interface ConfineMetaClassChanges {
  /**
   * The classes to restore the meta classes of.
   */
  Class<?>[] value();
}
