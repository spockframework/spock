package grails.plugin.spock.build

import spock.lang.Sputnik

class GrailsSpeckSuite {

    final specks = []
    
    def leftShift(Class speck) {
        specks << speck
    }

    int countTestCases() {
        specks.sum(0) {
            new Sputnik(it).description.testCount()
        }
    }
    
    int testCount() {
        specks.size()
    }
}