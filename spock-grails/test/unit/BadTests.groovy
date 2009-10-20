class BadTests extends GroovyTestCase {

    void testIt() {
        fail("bang")
    }
    
    void testIt2() {
        fail "bang"
    }
}