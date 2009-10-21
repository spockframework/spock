includeTargets << grailsScript("_GrailsCompile")

eventTestPhasesStart = { phases ->
    ["unit", "integration"].each { phase ->
        if (phase in phases) {
            phases.add(phases.indexOf(phase) + 1, "$phase-speck")
        }
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
    this.class.classLoader.loadClass("grails.plugin.spock.build.test.run.$it")
}