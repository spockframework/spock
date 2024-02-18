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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;
import spock.config.ConfigurationObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Configuration for the {@link spock.lang.Snapshot} extension.
 *
 * @since 2.4
 */
@Beta
@ConfigurationObject("snapshots")
public class SnapshotConfig {
  /**
   * Controls the where the snapshots are stored.
   */
  @Nullable
  public Path rootPath = Optional.ofNullable(System.getProperty("spock.snapshots.rootPath")).map(Paths::get).orElse(null);
  /**
   * Instructs the {@link spock.lang.Snapshotter} to update the snapshot instead of failing on a mismatch or missing snapshot.
   */
  public boolean updateSnapshots = Boolean.getBoolean("spock.snapshots.updateSnapshots");
  /**
   * Controls whether the {@link spock.lang.Snapshotter} should write actual value next to the snapshot file with the '.actual' extension.
   * <p>
   * The file will be deleted upon a successful match.
   */
  public boolean writeActualSnapshotOnMismatch = Boolean.getBoolean("spock.snapshots.writeActual");
  /**
   * The default extension to use.
   */
  public String defaultExtension = "txt";
}
