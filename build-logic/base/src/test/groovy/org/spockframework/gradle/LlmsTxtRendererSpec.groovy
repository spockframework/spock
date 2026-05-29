/*
 *  Copyright 2026 the original author or authors.
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
 */

package org.spockframework.gradle

import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class LlmsTxtRendererSpec extends Specification {
  static final String BASE_URL = 'https://spockframework.org/spock/docs/'

  @TempDir
  FileSystemFixture docs

  def setup() {
    docs.create {
      dir('2.4') {
        file('index.md') << '''\
          # Spock Framework Reference Documentation

          1. [Introduction](introduction.md): A brief overview of Spock.
          2. [Getting Started](getting_started.md): Quick start.
          3. [Known Issues](known_issues.md)

          [Single Page Documentation](all_in_one.md)
        '''.stripIndent(true)
      }
      ['2.3', '1.0', '2.4-M1', '2.5-SNAPSHOT', 'current', 'latest'].each { dir(it) {} }
    }
  }

  def "renders template, example and notes"() {
    when:
    def result = LlmsTxtRenderer.render(docs.currentPath, BASE_URL)

    then:
    with(result) {
      startsWith('# Spock Framework Documentation')
      contains("Template: ${BASE_URL}{version}/{page}.md")
      contains("${BASE_URL}2.4/all_in_one.md")
    }
  }

  def "renders pages from the latest stable index.md, slug + optional description, link stripped"() {
    when:
    def result = LlmsTxtRenderer.render(docs.currentPath, BASE_URL)

    then:
    result.contains('- `introduction` — A brief overview of Spock.')
    result.contains('- `getting_started` — Quick start.')

    and: "entry without description renders as a bare slug"
    result.contains('- `known_issues`\n')
    !result.contains('- `known_issues` —')

    and: "all_in_one is included"
    result.contains('- `all_in_one`')
  }

  def "renders the versions section with stable list and development snapshot"() {
    when:
    def result = LlmsTxtRenderer.render(docs.currentPath, BASE_URL)

    then:
    with(result) {
      contains('Latest stable: 2.4')
      contains('Stable: 2.4, 2.3, 1.0')
      contains('Development: 2.5-SNAPSHOT')
    }
  }

  def "omits stable lines and never emits 'null' when there are no stable versions"() {
    given: "a docs dir with only a development snapshot"
    def snapshotOnly = docs.currentPath.resolve('snap-only')
    java.nio.file.Files.createDirectories(snapshotOnly.resolve('2.5-SNAPSHOT'))

    when:
    def result = LlmsTxtRenderer.render(snapshotOnly, BASE_URL)

    then:
    !result.contains('null')
    !result.contains('Latest stable:')
    result.contains('Development: 2.5-SNAPSHOT')
    and: "the example falls back to the snapshot"
    result.contains("Example:  ${BASE_URL}2.5-SNAPSHOT/all_in_one.md")
  }

  def "emits no per-page absolute URLs (token efficiency)"() {
    when:
    def result = LlmsTxtRenderer.render(docs.currentPath, BASE_URL)

    then:
    !result.contains('2.4/introduction')
    !result.contains('2.4/getting_started')
  }
}
