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

// tag::example[]
  // all feature will share the same temp directory path1
  @TempDir
  @Shared
  Path path1

  // every iteration will have its own path2
  @TempDir
  File path2

  // will be injected using java.nio.file.Path
  @TempDir
  def path3
// end::example[]
}
