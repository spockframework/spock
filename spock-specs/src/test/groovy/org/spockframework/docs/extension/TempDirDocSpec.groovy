package org.spockframework.docs.extension

import spock.lang.*
import spock.util.io.FileSystemFixture

import java.nio.file.Path

/**
 * @author dqyuan
 * @since 2.0
 */
class TempDirDocSpec extends Specification {

// tag::example[]
  // all features will share the same temp directory path1
  @TempDir
  @Shared
  Path path1

  // all features and iterations will have their own path2
  @TempDir
  File path2

  // will be injected using java.nio.file.Path
  @TempDir
  def path3

  // use a custom class that accepts java.nio.file.Path as sole constructor parameter
  @TempDir
  FileSystemFixture path4

  // Use for parameter injection of a setupSpec method
  def setupSpec(@TempDir Path sharedPath) {
    assert sharedPath instanceof Path
  }

  // Use for parameter injection of a setup method
  def setup(@TempDir Path setupPath) {
    assert setupPath instanceof Path
  }

  // Use for parameter injection of a feature
  def demo(@TempDir Path path5) {
    expect:
    path1 instanceof Path
    path2 instanceof File
    path3 instanceof Path
    path4 instanceof FileSystemFixture
    path5 instanceof Path
  }

// end::example[]
}
