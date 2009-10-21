includeTargets << grailsScript("_GrailsCompile")

eventTestPhasesStart = { phases ->
    if ("unit" in phases) {
        phases.add(phases.indexOf('unit') + 1, 'unit-speck')
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

loadSpockClass = {
    this.class.classLoader.loadClass("grails.plugin.spock.build.test.run.$it")
}

