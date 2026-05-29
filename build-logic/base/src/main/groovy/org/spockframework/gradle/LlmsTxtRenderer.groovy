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

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher

/**
 * Renders a token-efficient {@code llms.txt} index for the published Spock documentation.
 *
 * <p>The file gives a URL template plus one example, the page list (slug + optional
 * one-sentence description, derived from the latest stable version's {@code index.md} with
 * link markup stripped), and a compact list of versions. It deliberately avoids spelling
 * out an absolute URL for every page and version.
 */
@CompileStatic
class LlmsTxtRenderer {
  // [Title](slug.md) optionally followed by ": description"
  private static final String PAGE_LINK_REGEX = /\[[^\]]+]\(([^)]+)\.md\)(?::\s*(.+))?/

  static String render(Path docsDir, String baseUrl) {
    def latestStable = DocVersions.latestStable(docsDir)
    // fall back to the snapshot (or a literal placeholder) so the example never reads ".../null/"
    def exampleVersion = latestStable ?: DocVersions.latestSnapshot(docsDir) ?: '{version}'

    // Template is written flush-left on purpose: stripIndent() runs after interpolation, so
    // indenting it would mangle the multi-line ${renderPages}/${renderVersions} blocks.
    """\
# Spock Framework Documentation

> Spock is a testing and specification framework for Java and Groovy applications.
> Compose a documentation page URL from the template below using any version and page slug.

Template: ${baseUrl}{version}/{page}.md
Example:  ${baseUrl}${exampleVersion}/all_in_one.md

## Pages

${renderPages(docsDir, latestStable)}

## Versions

${renderVersions(docsDir)}
"""
  }

  private static String renderPages(Path docsDir, String latestStable) {
    pages(docsDir, latestStable)
      .collect { Page page -> page.description ? "- `${page.slug}` — ${page.description}" : "- `${page.slug}`" }
      .join('\n')
  }

  private static String renderVersions(Path docsDir) {
    def stable = DocVersions.stable(docsDir)
    def lines = []
    if (stable) {
      lines << "- Latest stable: ${stable[0]}"
      lines << "- Stable: ${stable.join(', ')}"
    }
    def latestSnapshot = DocVersions.latestSnapshot(docsDir)
    if (latestSnapshot) {
      lines << "- Development: ${latestSnapshot}"
    }
    lines.join('\n')
  }

  private static List<Page> pages(Path docsDir, String latestStable) {
    if (!latestStable) {
      return []
    }
    def index = docsDir.resolve(latestStable).resolve('index.md')
    if (!Files.isRegularFile(index)) {
      return []
    }
    List<Page> pages = []
    index.eachLine { String line ->
      Matcher matcher = line =~ PAGE_LINK_REGEX
      if (matcher.find()) {
        def description = matcher.group(2)?.trim()
        pages << new Page(matcher.group(1), description ?: null)
      }
    }
    pages
  }

  @CompileStatic
  private static class Page {
    final String slug
    final String description

    Page(String slug, String description) {
      this.slug = slug
      this.description = description
    }
  }
}
