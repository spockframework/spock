package spock.util.injectmock

import spock.lang.Specification

class InjectMocksSettersNoCollaboratorsWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    public static final String TEST_METHOD_2 = "Test method 2"

    List<Integer> listNotToBeInjected = Mock()

    List<SomeOtherClass> someOtherClassNotToBeInjected = Mock()

    List<SomeOtherClass> someOtherClassToBeInjected = Mock()

    List<SomeOtherClass> someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject parameters into subject via setters"() {
        expect:
        objectUnderTest.someOtherClass == someOtherClass

        and:
        objectUnderTest.someOtherClassToBeInjected == someOtherClassToBeInjected
    }


    class SomeClass {
        List<SomeOtherClass> someOtherClass
        List<SomeOtherClass> someOtherClassToBeInjected
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


