package spock.util.injectmock

import spock.lang.Specification

class InjectMocksPropertiesNoCollaboratorsWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    public static final String TEST_METHOD_2 = "Test method 2"

    List<SomeOtherClass> listNotToBeInjected = []

    List<SomeOtherClass> someOtherClassToBeInjected = []

    List<SomeOtherClass> someOtherClass = []

	@Subject
	SomeClass objectUnderTest

    def "should inject fields into subject via properties with no collaborators"() {
        expect:
        objectUnderTest.someOtherClass == someOtherClass

        and:
        objectUnderTest.someOtherClassToBeInjected == someOtherClassToBeInjected
    }


    class SomeClass {
        private List<SomeOtherClass> someOtherClass
        private List<SomeOtherClass> someOtherClassToBeInjected
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


