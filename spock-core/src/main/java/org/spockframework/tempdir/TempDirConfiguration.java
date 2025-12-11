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

package org.spockframework.tempdir;

import spock.config.ConfigurationObject;
import spock.lang.TempDir;

import java.nio.file.Path;
import java.util.Optional;

/**
 *
 * Configuration settings for the TempDir extension.
 *
 * <p>Example:
 * <pre>
 * tempdir {
 *   // java.nio.Path object, default null,
 *   // which means system property "java.io.tmpdir"
 *   baseDir Paths.get("/tmp")
 *   // boolean, default is system property "spock.tempDir.keep"
 *   keep true
 * }
 * </pre>
 * @author dqyuan
 * @since 2.0
 */
@ConfigurationObject("tempdir")
public class TempDirConfiguration {

  /**
   * The parent directory for the temporary folder, default is system property {@code java.io.tmpdir}.
   */
  public Path baseDir = null;

  /**
   * Whether to keep the temp directory or not after test if it failed,
   * default is system property {@link TempDir#TEMP_DIR_CLEANUP_PROPERTY} or {@link TempDir.CleanupMode#ALWAYS} if it is not set.
   *
   * @see TempDir#cleanup()
   * @see TempDir#TEMP_DIR_CLEANUP_PROPERTY
   *
   * @since 2.3
   */
  public TempDir.CleanupMode cleanup = Optional.ofNullable(System.getProperty(TempDir.TEMP_DIR_CLEANUP_PROPERTY))
    .map(TempDir.CleanupMode::valueOf)
    .orElse(TempDir.CleanupMode.ALWAYS);
}
