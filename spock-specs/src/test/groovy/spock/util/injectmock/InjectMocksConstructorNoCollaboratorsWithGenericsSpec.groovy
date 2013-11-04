package spock.util.injectmock

import spock.lang.Specification

class InjectMocksConstructorNoCollaboratorsWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    List<SomeClass> listThatShouldNotBeInjected = []

    List<SomeOtherClass> someOtherClassList = [new SomeOtherClass()]

	@Subject
	SomeClass objectUnderTest

    def "should inject list of some other classes into subject"() {
        expect:
            objectUnderTest.someOtherClassList == someOtherClassList
    }


    class SomeClass {
        List<SomeOtherClass> someOtherClassList

        SomeClass(List<SomeOtherClass> someOtherClassList) {
            this.someOtherClassList = someOtherClassList
        }
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


