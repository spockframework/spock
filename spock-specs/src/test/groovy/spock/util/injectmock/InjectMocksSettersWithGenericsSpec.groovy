package spock.util.injectmock

import spock.lang.Specification

class InjectMocksSettersWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    public static final String TEST_METHOD_2 = "Test method 2"

    @Collaborator
    List<Integer> listNotToBeInjected = Mock()

    @Collaborator
    List<SomeOtherClass> someOtherClassNotToBeInjected = Mock()

    @Collaborator
    List<SomeOtherClass> someOtherClassToBeInjected = Mock()

    @Collaborator
    List<SomeOtherClass> someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator into subject via setters"() {
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


