package org.spockframework.gradle


import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import static java.nio.file.Files.list
import static org.spockframework.gradle.AsciiDocLinkVerifier.verifyAnchorlessCrossDocumentLinks
import static org.spockframework.gradle.AsciiDocLinkVerifier.verifyLinksAndAnchors

class AsciiDocLinkVerifierTest extends Specification {
  @TempDir
  FileSystemFixture tempDir

  def "anchorless cross document links are detected"() {
    given:
    tempDir.create {
      file('foo.adoc') << '''
        = Foo
        <<bar.adoc#,Bar>>
      '''.stripIndent(true)

      file('bar.adoc') << '''
        = Bar
        <<foo.adoc#,Foo>>
      '''.stripIndent(true)
    }

    when:
    verifyAnchorlessCrossDocumentLinks(list(tempDir.currentPath).map { it.toFile() }.toList())

    then:
    IllegalArgumentException e = thrown()
    e.message == '''
      bar.adoc contains a cross-document link to foo.adoc without anchor, this will break in one-page variant
      foo.adoc contains a cross-document link to bar.adoc without anchor, this will break in one-page variant
    '''.stripIndent(true).trim()
  }

  def "anchorless cross document links are accepted in index.adoc"() {
    given:
    tempDir.create {
      file('foo.adoc') << '''
        = Foo
      '''.stripIndent(true)

      file('index.adoc') << '''
        = Index
        <<foo.adoc#,Foo>>
      '''.stripIndent(true)
    }

    when:
    verifyAnchorlessCrossDocumentLinks(list(tempDir.currentPath).map { it.toFile() }.toList())

    then:
    noExceptionThrown()
  }

  def "problems with links and anchors are detected"() {
    given:
    tempDir.create {
      file('foo.html') << '''
        <a href="bar.html"/>
        <a href="baz.html#baz"/>
        <a href="#foo"/>
        <div id="foo1"/>
        <div id="foo1"/>
      '''.stripIndent(true)

      file('baz.html') << '''
      '''.stripIndent(true)
    }

    when:
    verifyLinksAndAnchors(list(tempDir.currentPath).map { it.toFile() }.toList())

    then:
    IllegalArgumentException e = thrown()
    e.message == '''
      foo.html contains a dead link to bar.html
      foo.html contains a dead anchor to baz.html#baz
      foo.html contains a dead anchor to foo
      foo.html contains multiple anchors with the name 'foo1'
    '''.stripIndent(true).trim()
  }
}
