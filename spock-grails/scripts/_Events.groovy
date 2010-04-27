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

loadSpecTestTypeClass = { ->
  def doLoad = { -> classLoader.loadClass('grails.plugin.spock.test.GrailsSpecTestType') }
  try {
    doLoad()
  } catch (ClassNotFoundException e) {
    includeTargets << grailsScript("_GrailsCompile") 
    compile()
    doLoad()
  }  
}

eventAllTestsStart = {
  def specTestTypeClass = loadSpecTestTypeClass()
  unitTests << specTestTypeClass.newInstance('spock', 'unit')
  integrationTests << specTestTypeClass.newInstance('spock', 'integration')
  functionalTests << specTestTypeClass.newInstance('spock', 'functional')
}

eventTestPhaseStart = { phaseName ->
  if (phaseName == 'functional') {
    
    // This is required when building with maven, to workaround a bug in grails-maven
    if (!binding.hasProperty('serverContextPath')) {
      includeTargets << grailsScript("_GrailsPackage") 
      configureServerContextPath()
    }
    
    def functionalSpecClass = classLoader.loadClass("grails.plugin.spock.FunctionalSpec")
    functionalSpecClass.baseUrl = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath"
  }
}