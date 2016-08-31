package spock.util.injectmock

import spock.lang.Specification

class InjectMocksSeveralConstructorsOneCollaboratorSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    String string = "string"

    Integer integer = 10

    SomeOtherClass someOtherClassNotToBeInjected = Mock()

    @Collaborator
    SomeOtherClass someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator into subject"() {
        given:
        someOtherClass.someMethod() >> TEST_METHOD_1

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()

        then:
        firstResult == TEST_METHOD_1
        objectUnderTest.someOtherClass == someOtherClass
        objectUnderTest.string == null
        objectUnderTest.integer == null
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


