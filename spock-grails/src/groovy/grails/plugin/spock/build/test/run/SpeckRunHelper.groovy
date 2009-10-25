/*
 * Copyright 2009 the original author or authors.
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

package grails.plugin.spock.build.test.run

import grails.util.BuildSettings
import org.spockframework.buildsupport.SpeckClassFileFinder

import grails.plugin.spock.build.test.adapter.SuiteAdapter

class SpeckRunHelper {
  protected final baseDir
  protected final testClassesDir
  protected final parentLoader
  protected final currentClassLoader
  protected final resourceResolver
  protected final speckFinder = new SpeckClassFileFinder()

  SpeckRunHelper(BuildSettings settings, ClassLoader classLoader, Closure resourceResolver) {
    this.baseDir = settings.baseDir
    this.testClassesDir = settings.testClassesDir
    this.parentLoader = classLoader
    this.resourceResolver = resourceResolver
  }

  def createTests(List<String> testNames, String type) {
    def testTypeClassesDir = "${testClassesDir.absolutePath}/$type"
    def testTypeClassesDirFile = new File(testTypeClassesDir)
    def suite = new SuiteAdapter()

    currentClassLoader = createClassLoader(type)

    def testResources = testNames.inject([]) {resources, filePattern ->

      // Filter out the method name or file extension
      int pos = filePattern.lastIndexOf('.')
      if (pos != -1) {
        filePattern = filePattern.substring(0, pos)
      }
      resources += resourceResolver("file:${testTypeClassesDir}/**/${filePattern}*.class") as List
    }

    def speckFiles = testResources*.file.findAll { speckFinder.isSpeck(it) }
    speckFiles.each {
      def className = fileToClassName(it, testTypeClassesDirFile)
      suite << currentClassLoader.loadClass(className)
    }

    suite
  }

  protected fileToClassName(File file, File baseDir) {
    def filePath = file.canonicalFile.absolutePath
    def basePath = baseDir.canonicalFile.absolutePath
    if (!filePath.startsWith(basePath))
      throw new IllegalArgumentException("File path (${filePath}) is not descendent of base path (${basePath}).")

    filePath = filePath.substring(basePath.size() + 1)
    def suffixPos = filePath.lastIndexOf(".")
    return filePath[0..(suffixPos - 1)].replace(File.separatorChar, '.' as char)
  }

  protected createClassLoader(String type) {
    def classPathAdditions = []
    classPathAdditions << new File("test/$type").toURI().toURL()
    classPathAdditions << new File(testClassesDir, type).toURI().toURL()

    new URLClassLoader(classPathAdditions as URL[], parentLoader)
  }
}