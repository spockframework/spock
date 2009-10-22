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

includeTargets << grailsScript("_GrailsCompile")

eventTestPhasesStart = {phases ->
  ["unit", "integration"].each {phase ->
    if (phase in phases)
      phases.add(phases.indexOf(phase) + 1, "$phase-speck")
  }
}

binding.'unit-speckTests' = ['unit']

binding.'unit-speckTestsPreparation' = {

  // Force a compile to make our classes available
  compile()

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpeckRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpeckRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'unit-speckTestsCleanUp' = {
  testRunner = previousTestRunner
}

binding.'integration-speckTests' = ['integration']

binding.'integration-speckTestsPreparation' = {
  integrationTestsPreparation()
  // Force a compile to make our classes available
  compile()

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpeckRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpeckRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'integration-speckTestsCleanUp' = {
  integrationTestsCleanUp()
  testRunner = previousTestRunner
}

loadSpockClass = {
  getClass().classLoader.loadClass("grails.plugin.spock.build.test.run.$it")
}