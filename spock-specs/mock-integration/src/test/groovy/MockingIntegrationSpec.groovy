import org.spockframework.mock.CannotCreateMockException
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification
import spock.mock.MockMakers

class MockingIntegrationSpec extends Specification {

  static final String TEST_TYPE = System.getProperty("org.spockframework.mock.testType", "plain");

  def "can mock interface"() {
    given:
    def list = Mock(List)

    when:
    list.add(1)

    then:
    1 * list.add(1)
  }

  @IgnoreIf({ TEST_TYPE == "plain" })
  def "can mock class when cglib or byte-buddy are present"() {
    given:
    def list = Mock(ArrayList)

    when:
    list.add(1)

    then:
    1 * list.add(1)
  }

  @Requires({ TEST_TYPE == "plain" })
  def "cannot mock class without cglib and byte-buddy"() {
    when:
    Mock(ArrayList)

    then:
    thrown(CannotCreateMockException)
  }

  @IgnoreIf({ TEST_TYPE == "plain" })
  def "can spy on class when cglib or byte-buddy are present"() {
    given:
    def list = Spy(ArrayList)

    when:
    list.add(1)

    then:
    1 * list.add(1)
    list.get(0) == 1
  }

  @Requires({ TEST_TYPE == "plain" })
  def "cannot spy on class without cglib and byte-buddy"() {
    when:
    Spy(ArrayList)

    then:
    thrown(CannotCreateMockException)
  }

  @Requires({ TEST_TYPE == "plain" })
  def "cannot Mock explicitly with byte-buddy, if it is not on the classpath"() {
    when:
    Mock(mockMaker: MockMakers.byteBuddy, Runnable)

    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for interface java.lang.Runnable. byte-buddy: The byte-buddy library is missing on the class path."
  }

  @Requires({ TEST_TYPE == "plain" })
  def "cannot Mock explicitly with cglib, if it is not on the classpath"() {
    when:
    Mock(mockMaker: MockMakers.cglib, Runnable)

    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for interface java.lang.Runnable. cglib: The cglib-nodep library is missing on the class path."
  }

}
