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
  ["unit", "integration", "functional"].each { phase ->
    if (phase in phases) {
      if (phase == 'functional') {
        // due to a 1.2-M3 bug, we have to evict the functional phase
        def i = phases.indexOf(phase)
        phases.remove(i)
        phases.add(i, "$phase-speck")
      } else {
        phases.add(phases.indexOf(phase) + 1, "$phase-speck")
      }
    }
  }
  
  if (new File("${basedir}/test/functional").exists()) {
    if (!('functional' in functionalTests)) functionalTests << ['functional']
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

binding.'functional-speckTests' = ['functional']

binding.'functional-speckTestsPreparation' = {
  functionalTestsPreparation()
  // Force a compile to make our classes available
  compile()
  
  def functionalSpecificationClass = getClass().classLoader.loadClass("grails.plugin.spock.FunctionalSpecification")
  functionalSpecificationClass.baseUrl = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath"

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpeckRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpeckRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'functional-speckTestsCleanUp' = {
  functionalTestsCleanUp()
  testRunner = previousTestRunner
}


loadSpockClass = {
  getClass().classLoader.loadClass("grails.plugin.spock.build.test.run.$it")
}

