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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.*
import spock.util.io.FileSystemFixture

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

  def "have non-writeable directory and file in temp directory"() {
    expect:
    if (i == 0) {
      def dir = Paths.get(iterationDir.toURI())
      def aaa = dir.resolve("aaa")
      def aaabbb = aaa.resolve("bbb")
      def tempFile = aaabbb.resolve("tmp.txt")
      Files.createDirectories(aaabbb)
      Files.write(tempFile, "ewfwf".bytes)
      aaabbb.toFile().writable = false
      aaa.toFile().writable = false
      tempFile.toFile().writable = false
      previousIteration = iterationDir
    } else if (i == 1) {
      assert !previousIteration.exists()
    }

    where:
    i << [0, 1]
  }

  def "define temp directory location using configuration script"() {
    given:
    def userDefinedBase = untypedPath.resolve("build")
    runner.configurationScript {
      tempdir {
        baseDir userDefinedBase
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
  temp.parent.fileName.toString() == "build"
}
""")

    then:
    result.testsSucceededCount == 1
  }

  static List<Path> pathFromEmbedded

  @ResourceLock(Resources.SYSTEM_ERR)
  def "cleanup mode can be controlled"(boolean testSucceeds, TempDir.CleanupMode cleanupMode, boolean expectKeep) {
    given:
    runner.addClassImport(Path)
    runner.addClassImport(getClass())
    runner.addClassMemberImport(TempDir.CleanupMode)
    runner.throwFailure = false

    and:
    pathFromEmbedded = []

    when:
    def result = runner.runSpecBody("""
@Shared
@TempDir(cleanup = ${cleanupMode})
Path sharedTemp

@TempDir(cleanup = ${cleanupMode})
Path temp

def setupSpec(@TempDir(cleanup = ${cleanupMode}) Path setupSpecParam) {
  TempDirExtensionSpec.pathFromEmbedded << setupSpecParam
}

def setup(@TempDir(cleanup = ${cleanupMode}) Path setupParam) {
  TempDirExtensionSpec.pathFromEmbedded << setupParam
}

def method1(@TempDir(cleanup = ${cleanupMode}) Path param) {
  given:
  TempDirExtensionSpec.pathFromEmbedded << sharedTemp
  TempDirExtensionSpec.pathFromEmbedded << temp
  TempDirExtensionSpec.pathFromEmbedded << param

  expect:
  $testSucceeds
}
""")

    then:
    result.testsSucceededCount == (testSucceeds ? 1 : 0)
    result.testsFailedCount == (testSucceeds ? 0 : 1)
    pathFromEmbedded.forEach {
      assert Files.exists(it) == expectKeep
    }

    cleanup:
    pathFromEmbedded.forEach {
      Files.deleteIfExists(it)
    }
    pathFromEmbedded = null

    where:
    [testSucceeds, cleanupMode] << [[true, false], TempDir.CleanupMode.values()].combinations()

    expectKeep = (!testSucceeds && cleanupMode == TempDir.CleanupMode.ON_SUCCESS) || cleanupMode == TempDir.CleanupMode.NEVER
  }

  @ResourceLock(Resources.SYSTEM_ERR)
  def "define cleanup using configuration script"(boolean testSucceeds, TempDir.CleanupMode cleanupMode, boolean expectKeep) {
    given:
    runner.configurationScript {
      tempdir {
        cleanup cleanupMode
      }
    }
    runner.addClassImport(Path)
    runner.addClassImport(getClass())
    runner.throwFailure = false

    and:
    pathFromEmbedded = []

    when:
    def result = runner.runSpecBody("""
@Shared
@TempDir
Path sharedTemp

@TempDir
Path temp

def setupSpec(@TempDir Path setupSpecParam) {
  TempDirExtensionSpec.pathFromEmbedded << setupSpecParam
}

def setup(@TempDir Path setupParam) {
  TempDirExtensionSpec.pathFromEmbedded << setupParam
}

def method1(@TempDir Path param) {
  given:
  TempDirExtensionSpec.pathFromEmbedded << sharedTemp
  TempDirExtensionSpec.pathFromEmbedded << temp
  TempDirExtensionSpec.pathFromEmbedded << param

  expect:
  $testSucceeds
}
""")

    then:
    result.testsSucceededCount == (testSucceeds ? 1 : 0)
    result.testsFailedCount == (testSucceeds ? 0 : 1)
    pathFromEmbedded.forEach {
      assert Files.exists(it) == expectKeep
    }

    cleanup:
    pathFromEmbedded.forEach {
      Files.deleteIfExists(it)
    }
    pathFromEmbedded = null

    where:
    [testSucceeds, cleanupMode] << [[true, false], TempDir.CleanupMode.values()].combinations()

    expectKeep = (!testSucceeds && cleanupMode == TempDir.CleanupMode.ON_SUCCESS) || cleanupMode == TempDir.CleanupMode.NEVER
  }
}

abstract class TempDirBaseSpec extends Specification {
  @TempDir Path tmp
  @TempDir @Shared Path sharedTmp
}

class TempDirInheritedSpec extends TempDirBaseSpec {
  @Issue("https://github.com/spockframework/spock/issues/1229")
  void "TempDir works for inherited fields"() {
    expect:
    tmp != null
  }

  @Issue("https://github.com/spockframework/spock/pull/1373")
  void "TempDir works for inherited shared fields"() {
    expect:
    sharedTmp != null
  }
}

@Issue("https://github.com/spockframework/spock/issues/1282")
class ParallelTempDirSpec extends Specification {

  @TempDir Path tmpDir

  def "Try creating multiple dirs"() {
    when:
    def dir = Files.createDirectory(tmpDir.resolve(name))

    then:
    Files.exists(dir)

    where:
    name << ["aaa", "bbb", "ccc"]
  }

  def "Try creating single dir"() {
    when:
    def dir = Files.createDirectory(tmpDir.resolve("foo"))

    then:
    Files.exists(dir)
  }

  def "Do nothing just test the temp dir existence"() {
    expect:
    Files.exists(tmpDir)
  }

  def "Do nothing just test the temp dir existence this time with unroll"() {
    expect:
    Files.exists(tmpDir)

    where:
    n << [1, 2, 3]
  }
}

class FileSystemFixtureSpec extends Specification {
  @TempDir
  MyFile myFile

  @TempDir
  MyPath myPath

  @TempDir
  FileSystemFixture fsFixture

  def "can use helper classes if they have a constructor accepting File or Path"() {
    expect:
    myPath.root != null
    myFile.root != null
    fsFixture.currentPath != null
  }

  def "FileSystemFixture can create a directory structure"() {
    when:
    fsFixture.create {
      dir('src') {
        dir('main') {
          dir('groovy') {
            file('HelloWorld.java') << 'println "Hello World"'
          }
        }
        dir('test/resources') {
          file('META-INF/MANIFEST.MF') << 'bogus entry'
        }
      }
    }

    then:
    Files.isDirectory(fsFixture.resolve('src/main/groovy'))
    Files.isDirectory(fsFixture.resolve('src/test/resources/META-INF'))
    fsFixture.resolve('src/main/groovy/HelloWorld.java').text == 'println "Hello World"'
    fsFixture.resolve('src/test/resources/META-INF/MANIFEST.MF').text == 'bogus entry'
  }

  def "can copy files from classpath"() {
    given:
    Path result = null
    Path result2 = null

    when:
    fsFixture.create {
      dir("target") {
        result = copyFromClasspath("/org/spockframework/smoke/extension/SampleFile.txt")
        result2 = copyFromClasspath("/org/spockframework/smoke/extension/SampleFile.txt", 'SampleFile2.txt')
      }
    }

    then:
    result.fileName.toString() == "SampleFile.txt"
    result.text == 'HelloWorld\n'
    result2.text == 'HelloWorld\n'
  }

  def "can copy files from classpath using explicit context class"() {
    given:
    Path result = null
    Path result2 = null

    when:
    fsFixture.create {
      dir("target") {
        result = copyFromClasspath("SampleFile.txt", FileSystemFixtureSpec)
        result2 = copyFromClasspath("SampleFile.txt", 'SampleFile2.txt', FileSystemFixtureSpec)
      }
    }

    then:
    result.fileName.toString() == "SampleFile.txt"
    result.text == 'HelloWorld\n'
    result2.text == 'HelloWorld\n'
  }
}

@Issue("https://github.com/spockframework/spock/issues/1518")
class CustomTempDirSpec extends Specification {
  @TempDir
  TempFile tmpFile

  @TempDir
  TempPath tmpPath

  def "can use custom dir objects"() {
    expect:
    tmpFile != null
    tmpPath != null
  }
}

class TempDirParameterSpec extends Specification {
  @Shared
  Path setupSpecParameter
  @Shared
  Path setupParameter
  @Shared
  def featureParameter

  def setupSpec(@TempDir Path tempDir) {
    setupSpecParameter = tempDir
  }

  def setup(@TempDir Path tempDir) {
    setupParameter = tempDir
  }

  def "test"(@TempDir tempDir) {
    given:
    featureParameter = tempDir
    expect:
    tempDir instanceof Path
    Files.isDirectory(setupSpecParameter)
    Files.isDirectory(setupParameter)
    Files.isDirectory(tempDir as Path)
  }

  def cleanupSpec() {
    assert !Files.exists(setupParameter)
    assert !Files.exists(featureParameter as Path)
  }
}

class MyFile {
  File root

  MyFile(File path) {
    this.root = path
  }
}


class MyPath {
 Path root

  MyPath(Path path) {
    this.root = path
  }
}

class TempFile extends File {
  TempFile(File parent) {
    super(parent.absolutePath)
  }
}

class TempPath implements Path {

  @Delegate
  Path delegate

  TempPath(Path delegate) {
    this.delegate = delegate
  }
}
