package spock.util.injectmock

import spock.lang.Specification

class InjectMocksConstructorWithGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    @Collaborator
    List<Integer> listNotToBeInjected = []

    @Collaborator
    List<SomeOtherClass> someOtherClassList = [new SomeOtherClass()]

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator list into subject"() {
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


