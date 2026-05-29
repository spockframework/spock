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

class DocsRedirectsSpec extends Specification {
  @TempDir
  FileSystemFixture docs

  def "index points straight to latest stable, current and latest are aliases"() {
    given:
    docs.create {
      ['1.0', '2.3', '2.4', '2.4-M1', '2.4-SNAPSHOT', '2.5-SNAPSHOT', 'current', 'latest'].each { dir(it) {} }
    }

    when:
    def redirects = DocsRedirects.redirects(docs.currentPath)

    then: "the main index redirects directly to the version (no current/ hop)"
    redirects['index.html'].contains('url=2.4/index.html')

    and:
    redirects['current/index.html'].contains('url=../2.4/index.html')
    redirects['latest/index.html'].contains('url=../2.5-SNAPSHOT/index.html')
  }

  def "redirect html uses a meta refresh to the given url"() {
    expect:
    DocsRedirects.redirectHtml('../2.4/index.html') == '''\
      <html>
        <head>
          <meta HTTP-EQUIV="REFRESH" content="0; url=../2.4/index.html">
        </head>
      </html>
    '''.stripIndent(true)
  }
}
