package org.spockframework.smoke.extension

import org.spockframework.util.Beta
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

/**
 * @author dqyuan
 */
@Beta
@Stepwise
class TempDirExtensionSpec extends Specification {

  @Shared
  @TempDir
  Path sharedDir
  static Path preShared

  @TempDir
  File iterationDir
  static File preIteration

  @TempDir(baseDir = "build")
  Path customerTempDir

  @TempDir(baseDir = "build", reserveAfterTest = { true })
  Path reserveDir
  static Path reserveFile

  def "temp dir exist"() {
    preShared = sharedDir
    preIteration = iterationDir
    reserveFile = reserveDir.resolve("test.txt")
    Files.write(reserveFile, "aaa".getBytes())
    expect:
    assert Files.exists(sharedDir)
    assert Files.exists(Paths.get(iterationDir.toURI()))
    assert Files.exists(customerTempDir)
    assert Files.exists(reserveDir)
    customerTempDir.parent.fileName == 'build'
    reserveDir.parent.fileName == 'build'
  }

  def "reserve temp directory when reserveAfterTest is true"() {
    expect:
    assert Files.exists(reserveFile)
  }

  def "@TempDir creates only one dir for one spec, if the annotated field is 'shared'"() {
    expect:
    assert sharedDir == preShared
    assert iterationDir != preIteration
    assert !Files.exists(Paths.get(preIteration.toURI()))
  }

  def "@TempDir creates one temp directory per iteration for normal field"(String foo, int bar) {
    expect:
    assert iterationDir != preIteration

    when:
    preIteration = iterationDir

    then:
    assert foo == "fo"
    assert bar == 1

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
      Files.createDirectories(dir.resolve("aaa").resolve("bbb"),
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-----")))
      Files.write(tempFile, "ewfwf".getBytes())
      Files.setPosixFilePermissions(aaabbb, PosixFilePermissions.fromString("r-xr-----"))
      Files.setPosixFilePermissions(aaa, PosixFilePermissions.fromString("r-xr-----"))
      preIteration = iterationDir
    } else if (i == 1) {
      assert !Files.exists(Paths.get(preIteration.toURI()))
    }

    where:
    i | _
    0 | _
    1 | _
  }

}
