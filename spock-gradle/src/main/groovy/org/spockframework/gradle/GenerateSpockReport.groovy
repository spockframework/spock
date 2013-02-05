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

package org.spockframework.gradle

import org.gradle.api.*
import org.gradle.api.tasks.*

class GenerateSpockReport extends DefaultTask {
  @Input
  String reportName

  @Input
  @Optional
  String reportFileName

  @Input
  @Optional
  Iterable<File> logFiles = []

  @Input
  @Optional
  Iterable<File> liveLogFiles = []

  @Input // should really be @InputDirectoryListing
  @Optional
  Iterable<File> logFileDirectories = []

  @OutputDirectory
  File outputDirectory

  @Input
  boolean local = true

  @Input
  boolean debug = true

  @InputFiles
  Iterable<File> spockReportClasspath = []

  @TaskAction
  void generate() {
    def classLoader = new URLClassLoader(getSpockReportClasspath().collect { it.toURI().toURL() } as URL[])
    def generator = classLoader.loadClass("org.spockframework.report.HtmlReportGenerator").newInstance()
    generator.reportName = getReportName()
    generator.reportFileName = getReportFileName()
    generator.logFiles = getLogFiles() +
        (getLogFileDirectories() as List).collectMany { (project.fileTree(it) as List) }
    generator.liveLogFiles = getLiveLogFiles()
    generator.outputDirectory = getOutputDirectory()
    generator.local = getLocal()
    generator.debug = isDebug()
    def reportFile = generator.generate()
    def reportUrl = classLoader.loadClass("org.spockframework.util.ConsoleUtil").asClickableFileUrl(reportFile)
    println "Spock report can be viewed at: $reportUrl"
  }
}
