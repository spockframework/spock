package spock.util.injectmock

import spock.lang.Specification

class InjectMocksSpeck extends Specification {

    public static final String TEST_METHOD_1 = "Test method 1"
    public static final String TEST_METHOD_2 = "Test method 2"
    public static final String TEST_METHOD_3 = "Yet another class - other method"

	@InjectMocks
	SomeClass objectUnderTest
	
    @Mock
    SomeOtherClass someOtherClass

    @Spy
    YetAnotherClass yetAnotherClass

    def "should inject mock and spy into object under test"() {
        given:
        someOtherClass.someMethod() >> TEST_METHOD_1
        yetAnotherClass.someMethod() >> TEST_METHOD_2

        when:
        String firstResult = objectUnderTest.someOtherClass.someMethod()
        String secondResult = objectUnderTest.yetAnotherClass.someMethod()
        String thirdResult = objectUnderTest.yetAnotherClass.someOtherMethod()

        then:
        firstResult == TEST_METHOD_1
        secondResult == TEST_METHOD_2
        thirdResult == TEST_METHOD_3
    }


    class SomeClass {
        SomeOtherClass someOtherClass
        YetAnotherClass yetAnotherClass
    }

    class SomeOtherClass {
        String someMethod() {
            "Some other class"
        }
    }

    class YetAnotherClass {
        String someMethod() {
            "Yet another class"
        }

        String someOtherMethod() {
            TEST_METHOD_3
        }
    }

}


