package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

import java.nio.file.Files
import java.nio.file.Path

/**
 * @author: dqyuan
 * @date: 2020/07/12
 */
class TempDirExtensionSpec extends EmbeddedSpecification {

  static List<Path> tmpPaths = []

  def setup() {
    tmpPaths.clear()
  }

  def assertTmpPathsNotExist() {
    tmpPaths.each {
      path -> assert !Files.exists(path)
    }
  }

  def "@TempDir creates only one dir for one spec, if the annotated field is 'shared'"() {
    when:
    def result = runner.runWithImports("""import spock.lang.Shared
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Stepwise
class Foo extends Specification {

  @Shared
  @TempDir
  Path dir1

  @Shared
  @TempDir
  File dir2

  static String testContent = UUID.randomUUID().toString()

  static String subFile = "dirtest.txt"

  def test1() {
    expect:
    Path testTxt = dir1.resolve(subFile)
    Files.write(testTxt, testContent.getBytes())
    File testTxt2 = new File(dir2, subFile)
    Files.write(Paths.get(testTxt2.toURI()), testContent.getBytes())
    org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths.add(dir1)
    org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths.add(Paths.get(dir2.toURI()))
  }

  def test2() {
    when:
    def txt1 = Files.readAllLines(dir1.resolve(subFile))[0]
    def txt2 = Files.readAllLines(Paths.get(dir2.toURI()).resolve(subFile))[0]

    then:
    txt1 == testContent
    txt2 == testContent
  }

}
""")

    then:
    result.testsStartedCount == 2
    result.testsSucceededCount == 2
    assertTmpPathsNotExist()
  }

  def "@TempDir creates one temp dir for one feature method, if the annotated field is not 'shared'"() {
    when:
    def result = runner.runWithImports("""import spock.lang.Shared
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Stepwise
class Foo extends Specification {

  @TempDir
  Path dir

  def test1() {
    expect:
    assert Files.exists(dir)
    org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths.add(dir)
  }

  def test2() {
    expect:
    Path prePath = org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths[0]
    assert prePath != dir
    assert !Files.exists(prePath)
    assert Files.exists(dir)
    org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths.add(dir)
  }

}
""")

    then:
    result.testsStartedCount == 2
    result.testsSucceededCount == 2
    assertTmpPathsNotExist()
  }

  def "have unwritable directory in temp directory"() {
    when:
    def result = runner.runWithImports("""import spock.lang.Shared
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFileAttributes
import java.nio.file.attribute.PosixFilePermissions

@Stepwise
class Foo extends Specification {

  @TempDir
  Path dir

  def test1() {
    expect:
    def aaa = dir.resolve("aaa")
    def aaabbb = aaa.resolve("bbb")
    def tempFile = aaabbb.resolve("tmp.txt")
    Files.createDirectories(dir.resolve("aaa").resolve("bbb"),
      PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-----")))
    Files.write(tempFile, "ewfwf".getBytes())
    Files.setPosixFilePermissions(aaabbb, PosixFilePermissions.fromString("r-xr-----"))
    Files.setPosixFilePermissions(aaa, PosixFilePermissions.fromString("r-xr-----"))
    org.spockframework.smoke.extension.TempDirExtensionSpec.tmpPaths.add(dir)
  }

}
""")

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 1
    assertTmpPathsNotExist()
  }

}
