/*
 *  Copyright 2025 the original author or authors.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.XmlSlurper
import org.ccil.cowan.tagsoup.Parser

@CompileStatic
class AsciiDocLinkVerifier {
  static verifyAnchorlessCrossDocumentLinks(Iterable<File> sourceFiles) {
    sourceFiles
      .collectMany { file ->
        if ((file.name == 'index.adoc') || (!file.name.endsWith('.adoc'))) {
          return []
        }

        return (file.text =~ /<<([^>#]+\.adoc)#,[^>]+>>/)
          .collect { List it -> it[1] }
          .unique()
          .collect {
            "$file.name contains a cross-document link to $it without anchor, this will break in one-page variant"
          }
      }
      .tap {
        if (it) {
          throw new IllegalArgumentException(it.join('\n'))
        }
      }
  }

  @CompileDynamic
  static verifyLinksAndAnchors(Iterable<File> outputFiles) {
    outputFiles
      .collectMany { file ->
        if (!file.name.endsWith('.html')) {
          return []
        }

        def xmlSlurper = new XmlSlurper(new Parser())

        def subject = xmlSlurper.parse(file)

        // collect all relative link targets
        def relativeLinkTargets = subject
          .'**'
          .findAll { it.name() == 'a' }
          *.@href
          *.text()
          .collect { URI.create(it) }
          .findAll {
            !it.scheme &&
              !it.authority &&
              !it.userInfo &&
              !it.host &&
              it.port == -1
          }

        // verify there are no dead links in the generated docs
        def result = relativeLinkTargets
          .findAll { it.path }
          *.path
          .findAll { !new File(file.parentFile, it).file }
          .collect { "$file.name contains a dead link to $it" }

        // verify there are no dead cross-document anchors in the generated docs
        result.addAll(
          relativeLinkTargets
            .findAll { it.path }
            .collect { linkTarget ->
              def linkTargetFile = new File(file.parentFile, linkTarget.path)
              if (!linkTargetFile.file || !linkTarget.fragment) {
                return
              }
              if (
                !xmlSlurper
                  .parse(linkTargetFile)
                  .'**'
                  .find { it.@id == linkTarget.fragment }
              ) {
                return "$file.name contains a dead anchor to $linkTarget"
              }
            }
            .findAll()
        )

        // verify there are no dead in-document anchors in the generated docs
        result.addAll(
          relativeLinkTargets
            .findAll { !it.path }
            *.fragment
            .findAll { fragment -> !subject.'**'.find { it.@id == fragment } }
            .collect { "$file.name contains a dead anchor to $it" }
            .findAll()
        )

        // verify there are no duplicate anchors in the generated docs
        return result + subject
          .'**'
          *.@id
          *.text()
          .findAll()
          .groupBy()
          .findAll { key, value -> value.size() > 1 }
          *.key
          .collect { "$file.name contains multiple anchors with the name '$it'" }
      }
      .tap {
        if (it) {
          throw new IllegalArgumentException(it.join('\n'))
        }
      }
  }
}
