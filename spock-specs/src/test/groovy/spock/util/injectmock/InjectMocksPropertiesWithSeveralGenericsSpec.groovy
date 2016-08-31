package spock.util.injectmock

import spock.lang.Specification

class InjectMocksPropertiesWithSeveralGenericsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    @Collaborator
    Map<String, Integer> mapNotToBeInjected = [:]

    @Collaborator
    Map<Integer, String> someOtherClassMap = [:]

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator list into subject"() {
        expect:
            objectUnderTest.someOtherClassMap == someOtherClassMap
    }


    class SomeClass {
        private Map<Integer, String> someOtherClassMap
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


