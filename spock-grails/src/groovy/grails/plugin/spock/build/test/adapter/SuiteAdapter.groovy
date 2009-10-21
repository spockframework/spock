package grails.plugin.spock.build.test.adapter

import spock.lang.Sputnik

class SuiteAdapter {

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