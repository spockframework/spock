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

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class GenerateDocsIndexTasksSpec extends Specification {
  @TempDir
  FileSystemFixture docs

  def setup() {
    docs.create {
      dir('2.4') {
        file('index.md') << '[Introduction](introduction.md): A brief overview.\n'
      }
      ['2.3', '2.5-SNAPSHOT'].each { dir(it) {} }
    }
  }

  def "GenerateLlmsTxt writes the rendered index to the output file"() {
    given:
    def project = ProjectBuilder.builder().build()
    def task = project.tasks.register('generateLlmsTxt', GenerateLlmsTxt).get()
    def output = docs.currentPath.resolve('llms.txt')

    and:
    task.docsDir.set(docs.currentPath.toFile())
    task.baseUrl.set('https://docs.spockframework.org/')
    task.outputFile.set(output.toFile())

    when:
    task.generate()

    then:
    output.text.contains('- `introduction` — A brief overview.')
    output.text.contains('Latest stable: 2.4')
  }

  def "GenerateDocsRedirects writes the index, current and latest redirect pages"() {
    given:
    def project = ProjectBuilder.builder().build()
    def task = project.tasks.register('generateDocsRedirects', GenerateDocsRedirects).get()

    and:
    task.docsDir.set(docs.currentPath.toFile())

    when:
    task.generate()

    then:
    docs.currentPath.resolve('index.html').text.contains('url=2.4/index.html')
    docs.currentPath.resolve('current/index.html').text.contains('url=../2.4/index.html')
    docs.currentPath.resolve('latest/index.html').text.contains('url=../2.5-SNAPSHOT/index.html')
  }
}
