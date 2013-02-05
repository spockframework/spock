/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.report

import org.spockframework.util.InternalSpockError
import org.spockframework.util.IoUtil

import java.util.regex.Pattern

/**
 * Generates an interactive, business-friendly, self-contained HTML report
 * that displays information about one or more spec executions. The report
 * data is based on report log files generated at spec execution time.
 */
class HtmlReportGenerator {
  private static final Pattern START_DELIMITER = Pattern.compile("<!--start (.*)-->")
  private static final Pattern END_DELIMITER = Pattern.compile("<!--end (.*)-->")

  /**
   * The name of the report, displayed at the top of the report's main page.
   * Defaults to {@code "Spock Report"}.
   */
  String reportName = "Spock Report"

  /**
   * The file name for the report's main page, which will be placed into
   * {@link #outputDirectory}. Defaults to {@code ${reportName}.html}.
   */
  String reportFileName

  /**
   * HTML that will show in the upper left corner. Could be your company logo etc.
   */
  String brand

  /**
   * The report log files, generated during spec execution, whose data is to
   * be displayed in the report. Can be used to generate a single report
   * from multiple independent spec executions.
   */
  Iterable<File> logFiles = []

  /**
   * Same as {@link #logFiles} except that the log files are kept at their
   * original location (rather than being copied into the output directory)
   * and do not have to exist.
   */
  Iterable<File> liveLogFiles = []

  /**
   * The output directory for the report. Besides the main page, this
   * directory will also contain all report resources (report log files,
   * images, style sheets, JavaScript libraries, etc.). If multiple reports are generated
   * into the same output directory, they will share their common resources.
   * Specifically, a resource file that already exists under the expected path will
   * be assumed to have the expected content, and will not be regenerated.
   */
  File outputDirectory

  /**
   * Whether to generate and link to local or remote (CDN) resources. Defaults to {@code true}.
   */
  boolean local = true

  /**
   * Whether to generate a report that is optimized for easy debugging, for example
   * by using non-minimized resources. Defaults to {@code true} (for the time being).
   */
  boolean debug = true

  /**
   * Generates the report.
   */
  File generate() {
    createOutputDirectory()
    copyLogFiles()
    generateStaticResources()
    generateReportFile()
  }

  private void createOutputDirectory() {
    IoUtil.createDirectory(outputDirectory)
  }

  private void copyLogFiles() {
    def targetDir = new File(outputDirectory, "logs")
    for (file in logFiles) {
      def targetFile = new File(targetDir, file.name)
      IoUtil.copyFile(file, targetFile)
    }
  }

  private void generateStaticResources() {
    def assets = new Assets(local, debug)

    assets.css.each {
      copyResource(it)
    }
    assets.img.each {
      copyResource(it)
    }
    assets.js.each {
      copyResource(it)
    }
  }

  private InputStream getResourceStream(String resourceName) {
    def resourcePath = "org/spockframework/report/$resourceName"
    def stream = getClass().classLoader.getResourceAsStream(resourcePath)
    if (stream == null) {
      throw new InternalSpockError("Failed to load class path resource '$resourcePath'")
    }
    stream
  }

  private void copyResource(String resourcePath) {
    if (resourcePath.startsWith("http://") || resourcePath.startsWith("https://")) return

    def source = getResourceStream(resourcePath)
    def target = new File(outputDirectory, resourcePath)
    if (!target.exists()) {
      target.parentFile.mkdirs()
      IoUtil.copyStream(source, new FileOutputStream(target))
    }
  }

  private File generateReportFile() {
    def assets = new Assets(local, debug)
    def source = getResourceStream("report.html")
    def target = new File(outputDirectory, reportFileName ?: "${reportName}.html")

    source.newReader("utf-8").withReader { BufferedReader bufReader ->
      target.withWriter("utf-8") { BufferedWriter bufWriter ->
        replaceDelimitedLines(bufReader, bufWriter, [
            pageTitle: ["<title>$reportName</title>"],
            brand: brand,
            reportTitle: ["""<span class="elementName">$reportName</span>"""],
            css: assets.css.collect { asset ->
              """<link href="$asset" rel="stylesheet" media="screen">"""
            },
            js: assets.js.collect { asset ->
              """<script src="$asset"></script>"""
            },
            logs: getLogFileIncludePaths().collect { path ->
              """<script src="$path"></script>"""
            }
        ])
      }
    }

    target
  }

  private List<String> getLogFileIncludePaths() {
    logFiles.name.collect { "logs/$it" } + liveLogFiles.collect { it.toURI().toURL().toString() }
  }

  private void replaceDelimitedLines(BufferedReader reader, BufferedWriter writer, Map replacements) {
    String currTag = null
    boolean skipping = false

    def line = reader.readLine()
    while (line != null) {
      def startMatcher = START_DELIMITER.matcher(line)
      if (startMatcher.matches()) {
        def startTag = startMatcher.group(1)
        if (currTag != null) {
          throw new IllegalArgumentException("Tag '$startTag' is nested inside '$currTag', but nesting is not allowed.")
        }
        if (!replacements.containsKey(startTag)) {
          throw new IllegalArgumentException("Missing replacement for tag '$startTag'")
        }
        currTag = startTag
        skipping = replacements[startTag] == null
        line = reader.readLine()
        continue
      }

      def endMatcher = END_DELIMITER.matcher(line)
      if (endMatcher.matches()) {
        def endTag = endMatcher.group(1)
        if (endTag != currTag) {
          throw new IllegalArgumentException("Expected end tag '$currTag' but got '$endTag'")
        }
        if (!skipping) {
          for (String newLine in replacements[endTag]) {
            writer.writeLine(newLine)
          }
        }
        currTag = null
        skipping = false
        line = reader.readLine()
        continue
      }

      if (currTag == null || skipping) writer.writeLine(line)
      line = reader.readLine()
    }
  }
}
