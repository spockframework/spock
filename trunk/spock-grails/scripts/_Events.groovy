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
        phases.add(i, "$phase-spec")
      } else {
        phases.add(phases.indexOf(phase) + 1, "$phase-spec")
      }
    }
  }
  
  if (new File("${basedir}/test/functional").exists()) {
    if (!('functional' in functionalTests)) functionalTests << ['functional']
  }
}

binding.'unit-specTests' = ['unit']

binding.'unit-specTestsPreparation' = {

  // Force a compile to make our classes available
  compile()

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpecRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpecRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'unit-specTestsCleanUp' = {
  testRunner = previousTestRunner
}

binding.'integration-specTests' = ['integration']

binding.'integration-specTestsPreparation' = {
  integrationTestsPreparation()
  // Force a compile to make our classes available
  compile()

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpecRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpecRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'integration-specTestsCleanUp' = {
  integrationTestsCleanUp()
  testRunner = previousTestRunner
}

binding.'functional-specTests' = ['functional']

binding.'functional-specTestsPreparation' = {
  functionalTestsPreparation()
  // Force a compile to make our classes available
  compile()
  
  def functionalSpecificationClass = getClass().classLoader.loadClass("grails.plugin.spock.FunctionalSpecification")
  functionalSpecificationClass.baseUrl = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath"

  previousTestRunner = testRunner
  testRunner = loadSpockClass('SpecRunner').newInstance(testReportsDir, reportFormats)

  loadSpockClass('SpecRunHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'functional-specTestsCleanUp' = {
  functionalTestsCleanUp()
  testRunner = previousTestRunner
}


loadSpockClass = {
  getClass().classLoader.loadClass("grails.plugin.spock.build.test.run.$it")
}

