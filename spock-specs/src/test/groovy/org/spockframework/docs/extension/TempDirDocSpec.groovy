package org.spockframework.docs.extension

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * @author dqyuan
 * @since 2.0
 */
// tag::example-common[]
class TempDirDocSpec extends Specification {
// end::example-common[]

// tag::example-a[]
  // all feature will share the same temp directory path1
  @TempDir
  @Shared
  Path path1

  // every iteration have its own path2
  @TempDir
  Path path2
}
// end::example-a[]

class TempDirParamSpec extends Specification {

// tag::example-b[]
  // generate temp directory in "build" directory relative to project path
  // and keep it after test
  @TempDir(baseDir = "build", reserveAfterTest = {true})
  Path tempPath
}
// end::example-b[]
