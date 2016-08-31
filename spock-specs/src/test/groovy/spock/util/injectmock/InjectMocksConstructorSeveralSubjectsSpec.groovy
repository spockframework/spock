package spock.util.injectmock

import spock.lang.Specification

class InjectMocksConstructorSeveralSubjectsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    @Collaborator
    SomeOtherClass someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    @Subject
	SomeClass objectUnderTest2

    def "should inject collaborator into each subject"() {
        given:
        someOtherClass.someMethod() >> TEST_METHOD_1

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()
        String secondResult = objectUnderTest2.someOtherClass.someMethod()

        then:
        firstResult == TEST_METHOD_1
        objectUnderTest.someOtherClass == someOtherClass

        and:
        secondResult == TEST_METHOD_1
        objectUnderTest2.someOtherClass == someOtherClass
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


