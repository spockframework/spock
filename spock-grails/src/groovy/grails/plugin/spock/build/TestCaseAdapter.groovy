package grails.plugin.spock.build

import junit.framework.TestCase
import org.junit.runner.Description

/**
 * Only used to adapt the JUnit 4 RunListener interface
 * to the JUnit 3 TestListener interface.
 */
class TestCaseAdapter extends TestCase {

    TestCaseAdapter(Description description) {
        super(description.methodName)
    }

}