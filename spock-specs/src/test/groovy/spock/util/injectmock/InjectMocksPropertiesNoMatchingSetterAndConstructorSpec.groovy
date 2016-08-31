package spock.util.injectmock

import spock.lang.Specification

class InjectMocksPropertiesNoMatchingSetterAndConstructorSpec extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"

    public static final String TEST_METHOD_2 = "Test method 2"

    @Collaborator
    SomeOtherClass someOtherClassToBeInjected = Mock()

    @Collaborator
    SomeOtherClass someOtherClass = Mock()

	@Subject
	SomeClass objectUnderTest

    def "should inject collaborator into subject via properties"() {
        given:
        someOtherClass.someMethod() >> TEST_METHOD_1
        someOtherClassToBeInjected.someMethod() >> TEST_METHOD_2

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()
        String secondResult = objectUnderTest.someOtherClassToBeInjected.someMethod()

        then:
        firstResult == TEST_METHOD_1
        objectUnderTest.someOtherClass == someOtherClass

        and:
        secondResult == TEST_METHOD_2
        objectUnderTest.someOtherClassToBeInjected == someOtherClassToBeInjected
    }


    class SomeClass {
        private SomeOtherClass someOtherClass
        private SomeOtherClass someOtherClassToBeInjected

        SomeClass() {

        }

        SomeClass(Integer integer) {

        }

        public void setBigDecimal(BigDecimal bigDecimal) {

        }
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

}


