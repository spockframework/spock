package spock.util.injectmock

import spock.lang.Specification

class InjectMocksConstructorNoCollaboratorsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    SomeClass someClass = Mock()

    SomeOtherClass someOtherClassToBeInjected = Mock()

    SomeOtherClass someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject first acceptable object into subject"() {
        given:
        someOtherClassToBeInjected.someMethod() >> TEST_METHOD_1

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()

        then:
        firstResult == TEST_METHOD_1
        objectUnderTest.someOtherClass == someOtherClassToBeInjected
    }


    class SomeClass {
        SomeOtherClass someOtherClass

        SomeClass(SomeOtherClass someOtherClass) {
            this.someOtherClass = someOtherClass
        }
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }


}


