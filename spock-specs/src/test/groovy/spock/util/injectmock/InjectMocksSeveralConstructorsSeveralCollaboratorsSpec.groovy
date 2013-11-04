package spock.util.injectmock

import spock.lang.Specification

class InjectMocksSeveralConstructorsSeveralCollaboratorsSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    @Collaborator
    String string = "string"

    @Collaborator
    Integer integer = 10

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    @Collaborator
    SomeOtherClass someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborators into subject"() {
        given:
        someOtherClass.someMethod() >> TEST_METHOD_1

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()

        then:
        firstResult == TEST_METHOD_1
        objectUnderTest.someOtherClass == someOtherClass
        objectUnderTest.string == string
        objectUnderTest.integer == integer
    }


    class SomeClass {
        SomeOtherClass someOtherClass
        String string
        Integer integer

        SomeClass(SomeOtherClass someOtherClass) {
            this.integer = integer
            this.string = string
            this.someOtherClass = someOtherClass
        }

        SomeClass(SomeOtherClass someOtherClass, String string) {
            this.integer = integer
            this.string = string
            this.someOtherClass = someOtherClass
        }

        SomeClass(SomeOtherClass someOtherClass, String string, Integer integer) {
            this.integer = integer
            this.string = string
            this.someOtherClass = someOtherClass
        }
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


