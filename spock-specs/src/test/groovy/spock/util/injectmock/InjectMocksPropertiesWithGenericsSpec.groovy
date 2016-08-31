package spock.util.injectmock

import spock.lang.Specification

class InjectMocksPropertiesWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    public static final String TEST_METHOD_2 = "Test method 2"

    List<SomeOtherClass> someOtherClassNonCollaboratorNotToBeInjected

    @Collaborator
    List<Integer> listNotToBeInjected = Mock()

    @Collaborator
    List<SomeOtherClass> someOtherClassToBeInjected = Mock()

    @Collaborator
    List<SomeOtherClass> someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator into subject via properties"() {
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


