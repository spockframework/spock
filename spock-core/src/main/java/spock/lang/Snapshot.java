/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.SnapshotExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;

/**
 * Injects a {@link Snapshotter} instance into a field or parameter.
 * <p>
 * It can also inject subclasses of {@link Snapshotter},
 * as long as they have a public constructor with a single {@link Snapshotter.Store} parameter.
 * <p>
 * Default values are configured via the {@link org.spockframework.runtime.extension.builtin.SnapshotConfig}.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@ExtensionAnnotation(SnapshotExtension.class)
public @interface Snapshot {
  /**
   * The file extension to use for the snapshot files.
   * Defaults to what is configured in the {@link org.spockframework.runtime.extension.builtin.SnapshotConfig}.
   */
  String extension() default "<default>";

  /**
   * The name of the {@link Charset} to use when storing and reading the snapshot.
   * Defaults to "UTF-8".
   */
  String charset() default "UTF-8";
}
