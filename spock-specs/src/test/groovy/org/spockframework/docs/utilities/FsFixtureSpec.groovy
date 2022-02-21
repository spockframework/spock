package org.spockframework.docs.utilities

import spock.lang.Specification
import spock.lang.TempDir
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
        }
      }
    }

    then:
    Files.isDirectory(fsFixture.resolve('src/main/groovy'))
    Files.isDirectory(fsFixture.resolve('src/test/resources/META-INF'))
    fsFixture.resolve('src/main/groovy/HelloWorld.java').toFile().text == 'println "Hello World"'
    fsFixture.resolve('src/test/resources/META-INF/MANIFEST.MF').toFile().text == 'bogus entry'
  }
// end::fs-fixture-usage[]


}
