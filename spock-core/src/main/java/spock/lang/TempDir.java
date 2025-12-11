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

import spock.util.io.FileSystemFixture;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TempDirExtension;

/**
 * Generate a temp directory for test, and delete it after test.
 *
 * <p>
 * {@code @TempDir} can be used to annotate a member field of type {@link File}, {@link Path},
 * or untyped like {@code def}/{@link Object} in a spec class (untyped field will be injected with {@code Path}).
 * <p>
 * Alternatively, you can use it with any class that has a public constructor with a single {@link File} or {@link Path}
 * parameter, like {@link FileSystemFixture} this way you can use your own utility classes for file manipulation.
 * <p>
 * If the annotated field is shared, the temp directory will be shared in this spec,
 * otherwise every iteration will have its own temp directory.
 * <p>
 * Example:
 * <pre><code>
 * &#64;TempDir
 * File testFile // will inject a File
 *
 * &#64;TempDir
 * Path testPath // will inject a Path
 *
 * &#64;TempDir
 * def testPath // will inject a Path
 *
 * &#64;TempDir
 * FileSystemFixture fsFixture // will inject an instance of FileSystemFixture with the temp path injected via constructor
 * </code></pre>
 * <p>
 * Since Spock 2.4, {@code @TempDir} can be used to annotate a method parameter, with the same behavior as the field types.
 * Use this if you want to use a temp directory in a single method only.
 * Valid methods are {@code setup()}, {@code setupSpec()}, or any feature methods.
 *
 * @author dqyuan
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@ExtensionAnnotation(TempDirExtension.class)
public @interface TempDir {
  String TEMP_DIR_CLEANUP_PROPERTY = "spock.tempdir.cleanup";

  /**
   * Whether to cleanup the directory after the test.
   *
   * @since 2.3
   */
  CleanupMode cleanup() default CleanupMode.DEFAULT;

  enum CleanupMode {
    /**
     * Use the default cleanup mode, configured via {@link #TEMP_DIR_CLEANUP_PROPERTY} or {@link org.spockframework.tempdir.TempDirConfiguration#cleanup}.
     */
    DEFAULT,
    /**
     * Always cleanup the directory after the test.
     */
    ALWAYS,
    /**
     * Cleanup the directory only if the test has succeeded.
     */
    ON_SUCCESS,
    /**
     * Never cleanup the directory after the test.
     */
    NEVER
  }
}
