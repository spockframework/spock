package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import spock.config.ConfigurationObject

import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationObject("snapshots")
@CompileStatic
class SnapshotConfig {
  Path rootPath = System.getProperty("spock.snapshots.rootPath")?.with { Paths.get(it) }
  boolean updateSnapshots = Boolean.getBoolean("spock.snapshots.updateSnapshots")
}
