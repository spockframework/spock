package org.spockframework.docs.extension

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * @author dqyuan
 * @since 2.0
 */
class TempDirDocSpec extends Specification {

  def placeholder() {
    expect: true
  }

// tag::example-a[]
  // all feature will share the same temp directory path1
  @TempDir
  @Shared
  Path path1

  // every iteration will have its own path2
  @TempDir
  Path path2

  // will be injected using java.nio.file.Path
  @TempDir
  def path3
// end::example-a[]
}


class TempDirParamSpec extends Specification {

  def placeholder() {
    expect:true
  }

// tag::example-b[]
  // generate temp directory in "build" directory relative to project path
  // and keep it after test
  @TempDir
  Path tempPath
// end::example-b[]
}

