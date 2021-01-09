package org.spockframework.spring

import org.spockframework.util.ReflectionUtil
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import javax.inject.Inject

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD

class SharedFieldsInjection extends Specification {

  EmbeddedSpecRunner runner = new EmbeddedSpecRunner()

  def setup() {
    runner.addClassImport(ContextConfiguration)
    runner.addClassImport(Inject)
    runner.addClassImport(EnableSharedInjection)
    runner.addClassImport(IService1)
  }

  def "shared fields cannot be injected by default"() {
    when:
    runner.runWithImports """
      @ContextConfiguration
      class Foo extends Specification {
        @$ann
        @Shared
        IService1 sharedService

        def foo() {
          expect: true
        }
      }
    """

    then:
    def e = thrown(SpringExtensionException)
    e.message == "@Shared field injection is not enabled by default therefore 'sharedService' field cannot be injected. " +
      "Refer to javadoc of ${EnableSharedInjection.name} for information on how to opt-in for @Shared field injection."

    where:
    ann << [
      "org.springframework.beans.factory.annotation.Autowired",
      "javax.annotation.Resource",
      "javax.inject.Inject"
    ].findAll(ReflectionUtil.&isClassAvailable)
  }

  def "shared fields can be injected if opted-in for"() {
    when:
    runner.runWithImports '''
      @ContextConfiguration(locations = "/org/spockframework/spring/InjectionExamples-context.xml")
      @EnableSharedInjection
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        def setupSpec() {
          assert sharedService != null
        }

        def test() {
          expect:
          sharedService != null
        }
      }
    '''

    then:
    noExceptionThrown()
  }

  def "shared fields can be injected if opted-in for on a superclass"() {
    when:
    runner.runWithImports '''
      @EnableSharedInjection
      abstract class AbstractSharedInjectionSpec extends Specification {
        @Inject
        @Shared
        IService1 sharedService
      }

      @ContextConfiguration(locations = "/org/spockframework/spring/InjectionExamples-context.xml")
      class SharedInjectionSpec extends AbstractSharedInjectionSpec {
        def setupSpec() {
          assert sharedService != null
        }

        def test() {
          expect:
          sharedService != null
        }
      }
    '''

    then:
    noExceptionThrown()
  }

  def "shared fields cannot be injected if a feature method dirties context"() {
    when:
    runner.runWithImports '''
      import org.springframework.test.annotation.DirtiesContext

      @ContextConfiguration
      @EnableSharedInjection
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        @DirtiesContext
        def test() {
          expect: true
        }
      }
    '''

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Shared field injection is not supported if feature methods make context dirty by using " +
      "@DirtiesContext annotation"
  }

  def "shared fields cannot be injected if all feature methods dirty context"() {
    when:
    runner.runWithImports """
      import org.springframework.test.annotation.DirtiesContext

      import static org.springframework.test.annotation.DirtiesContext.ClassMode.*

      @ContextConfiguration
      @EnableSharedInjection
      @DirtiesContext(classMode = ${classMode})
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        def test() {
          expect: true
        }
      }
    """

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Shared field injection is not supported if feature methods make context dirty by using " +
      "@DirtiesContext annotation"

    where:
    classMode << [BEFORE_EACH_TEST_METHOD, AFTER_EACH_TEST_METHOD]
  }

  def "shared fields can be injected if context is dirtied but only by spec and not feature methods"() {
    when:
    runner.runWithImports """
      import org.springframework.test.annotation.DirtiesContext

      @ContextConfiguration(locations = "/org/spockframework/spring/InjectionExamples-context.xml")
      @EnableSharedInjection
      @DirtiesContext
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        def setupSpec() {
          assert sharedService != null
        }

        def test() {
          expect:
          sharedService != null
        }
      }
    """

    then:
    noExceptionThrown()
  }

  def "shared fields cannot be injected if the spec is transactional"() {
    when:
    runner.runWithImports """
      import org.springframework.transaction.annotation.Transactional

      @ContextConfiguration
      @EnableSharedInjection
      @Transactional
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        def test() {
          expect: true
        }
      }
    """

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Shared field injection is not supported together with the use of @Transactional"
  }

  def "shared fields cannot be injected if a feature is transactional"() {
    when:
    runner.runWithImports """
      import org.springframework.transaction.annotation.Transactional

      @ContextConfiguration
      @EnableSharedInjection
      class Foo extends Specification {
        @Inject
        @Shared
        IService1 sharedService

        @Transactional
        def test() {
          expect: true
        }
      }
    """

    then:
    def e = thrown(SpringExtensionException)
    e.message == "Shared field injection is not supported together with the use of @Transactional"
  }
}
