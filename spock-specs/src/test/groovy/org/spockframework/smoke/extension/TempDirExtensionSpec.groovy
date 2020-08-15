package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author dqyuan
 */
@Stepwise
class TempDirExtensionSpec extends EmbeddedSpecification {

  @Shared
  @TempDir
  Path sharedDir
  @Shared
  Path previousShared

  @TempDir
  File iterationDir
  @Shared
  File previousIteration

  @TempDir
  def untypedPath

  def "temp dir exist"() {
    previousShared = sharedDir
    previousIteration = iterationDir

    expect:
    Files.exists(sharedDir)
    iterationDir.exists()
    iterationDir.directory
    untypedPath instanceof Path
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

  def "@TempDir creates one temp directory per iteration for normal field"() {
    expect:
    iterationDir != previousIteration

    cleanup:
    previousIteration = iterationDir

    where:
    i << (1..3)
  }

  def "have unwritable directory and file in temp directory"() {
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
      tempFile.toFile().setWritable(false)
      previousIteration = iterationDir
    } else if (i == 1) {
      assert !previousIteration.exists()
    }

    where:
    i << [0, 1]
  }

  static Path pathFromEmbedded

  def "define temp directory location and keep temp directory using configuration script"() {
    given:
    def userDefinedBase = Paths.get("build")
    runner.configurationScript {
      tempdir {
        baseDir userDefinedBase
        keep true
      }
    }
    runner.addClassImport(Path)
    runner.addClassImport(getClass())

    when:
    def result = runner.runSpecBody("""

@TempDir
Path temp

def method1() {
  expect:
  temp.getParent().getFileName().toString() == "build"

  cleanup:
  TempDirExtensionSpec.pathFromEmbedded = temp
}
""")

    then:
    result.testsSucceededCount == 1
    Files.exists(pathFromEmbedded)

    cleanup:
    Files.delete(pathFromEmbedded)
  }

}
