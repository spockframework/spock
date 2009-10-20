import grails.plugin.spock.build.GrailsSpeckHelper
import grails.plugin.spock.build.GrailsSpeckRunner

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

eventTestSuiteStart = { String type ->
    if (type == "functional") {
        testingBaseURL = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath"
        System.setProperty("grails.functional.test.baseURL", testingBaseURL)
    }
}

binding.'unit-speckTestsPreparation' = {
    previousTestRunner = testRunner
    testRunner = new GrailsSpeckRunner(testReportsDir, reportFormats)
    new GrailsSpeckHelper(grailsSettings, classLoader, resolveResources)
}

binding.'unit-speckTestsCleanUp' = {
    testRunner = previousTestRunner
}

