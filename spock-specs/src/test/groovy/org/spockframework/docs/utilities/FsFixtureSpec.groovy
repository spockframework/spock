package org.spockframework.docs.utilities

import spock.lang.*
import spock.util.io.FsFixture

import java.nio.file.Files

class FsFixtureSpec extends Specification {

// tag::fs-fixture-usage[]
  @TempDir
  FsFixture fsFixture

  def "FsFixture can create a directory structure"() {
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
          copyFromClasspath('/org/spockframework/smoke/extension/SampleFile.txt')
        }
      }
    }

    then:
    Files.isDirectory(fsFixture.resolve('src/main/groovy'))
    Files.isDirectory(fsFixture.resolve('src/test/resources/META-INF'))
    fsFixture.resolve('src/main/groovy/HelloWorld.java').text == 'println "Hello World"'
    fsFixture.resolve('src/test/resources/META-INF/MANIFEST.MF').text == 'bogus entry'
    fsFixture.resolve('src/test/resources/SampleFile.txt').text == 'HelloWorld\n'
  }
// end::fs-fixture-usage[]


}
