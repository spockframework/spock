package org.spockframework.smoke.extension


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author dqyuan
 */
@Stepwise
class TempDirExtensionSpec extends Specification {

  @Shared
  @TempDir
  Path sharedDir
  @Shared
  Path previousShared

  @TempDir
  File iterationDir
  @Shared
  File previousIteration

  @TempDir(baseDir = "build")
  Path customerTempDir

  @TempDir(baseDir = "build", keep = { true })
  Path reserveDir

  @Shared
  Path reserveFile

  @TempDir
  def untypedPath

  def "temp dir exist"() {
    previousShared = sharedDir
    previousIteration = iterationDir
    reserveFile = reserveDir.resolve("test.txt")
    Files.write(reserveFile, "aaa".getBytes())
    expect:
    Files.exists(sharedDir)
    iterationDir.exists()
    Files.exists(customerTempDir)
    Files.exists(reserveDir)
    untypedPath instanceof Path
    customerTempDir.parent.fileName.toString() == 'build'
    reserveDir.parent.fileName.toString() == 'build'
  }

  def "reserve temp directory when reserveAfterTest is true"() {
    expect:
    Files.exists(reserveFile)
  }

  def "@TempDir creates only one dir for one spec, if the annotated field is 'shared'"() {
    expect:
    sharedDir == previousShared
  }

  def "get a different path for non-shared field, and previous was deleted"() {
    expect:
    iterationDir != previousIteration
    !previousIteration.exists()
  }

  def "@TempDir creates one temp directory per iteration for normal field"(String foo, int bar) {
    expect:
    iterationDir != previousIteration
    foo == "fo"
    bar == 1

    cleanup:
    previousIteration = iterationDir

    where:
    foo  | bar
    "fo" | 1
    "fo" | 1
    "fo" | 1
  }

  def "have unwritable directory in temp directory"(int i) {
    expect:
    if (i == 0) {
      def dir = Paths.get(iterationDir.toURI())
      def aaa = dir.resolve("aaa")
      def aaabbb = aaa.resolve("bbb")
      def tempFile = aaabbb.resolve("tmp.txt")
      Files.createDirectories(aaabbb)
      Files.write(tempFile, "ewfwf".getBytes())
      aaabbb.toFile().setWritable(false)
      aaa.toFile().setWritable(false)
      previousIteration = iterationDir
    } else if (i == 1) {
      assert !previousIteration.exists()
    }

    where:
    i | _
    0 | _
    1 | _
  }

}
