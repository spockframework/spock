includeTargets << grailsScript("_GrailsCompile")

binding.'unit-speckTests' = ['unit']

eventTestPhasesStart = { phases ->
    if (!argsMap["unit"] && "unit" in phases) { // running all tests
        if (argsMap["unit-speck"]) {
            phases.clear()
            phases << 'unit-speck'
        } else {
            phases.add(phases.indexOf('unit') + 1, 'unit-speck')
        }
    }
}

binding.'unit-speckTestsPreparation' = {
    
    // Force a compile to make our classes available
    compile()
    
    previousTestRunner = testRunner
    testRunner = loadSpockClass('GrailsSpeckRunner').newInstance(testReportsDir, reportFormats)
    
    loadSpockClass('GrailsSpeckHelper').newInstance(grailsSettings, classLoader, resolveResources)
}

binding.'unit-speckTestsCleanUp' = {
    testRunner = previousTestRunner
}

loadSpockClass = {
    this.class.classLoader.loadClass("grails.plugin.spock.build.$it")
}

