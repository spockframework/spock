package spock.testkit.testsources

class InheritedChildTestCase extends InheritedParentTestCase {

  def 'Test defined in child'() {
    expect: 1 == 1
  }
}
