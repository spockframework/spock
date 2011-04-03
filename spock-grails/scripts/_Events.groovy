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

loadSpockTestTypes = {
  if (!binding.variables.containsKey("unitTests")) return
  def specTestTypeClass = loadSpecTestTypeClass()
  [unit: unitTests, integration: integrationTests].each { name, types ->
    if (!types.any { it.class == specTestTypeClass }) {
      types << specTestTypeClass.newInstance('spock', name)
    }
  }
}

eventAllTestsStart = {
  loadSpockTestTypes()
}

eventPackagePluginsEnd = {
  loadSpockTestTypes()
}

eventDefaultStart = {
    createUnitTest = { Map args = [:] ->
        def superClass
        // map unit test superclass to Spock equivalent
        switch(args["superClass"]) {
            case "ControllerUnitTestCase":
                superClass = "ControllerSpec"
                break
            case "TagLibUnitTestCase":
                superClass = "TagLibSpec"
                break
            default:
                superClass = "UnitSpec"
        }
        createArtifact name: args["name"], suffix: "${args['suffix']}Spec", type: "Spec", path: "test/unit", superClass: superClass
    }

    createIntegrationTest = { Map args = [:] ->
        createArtifact name: args["name"], suffix: "${args['suffix']}Spec", type: "Spec", path: "test/integration", superClass: "IntegrationSpec"
    }
}

// Just upgrade plugins without user input when building this plugin
// Has no effect for clients of this plugin
if (grailsAppName == 'spock-grails') {
  def resolveDependenciesWasInteractive = false
  eventResolveDependenciesStart = {
      resolveDependenciesWasInteractive = isInteractive
      isInteractive = false
  }

  eventResolveDependenciesEnd = {
      isInteractive = resolveDependenciesWasInteractive
  }
}
