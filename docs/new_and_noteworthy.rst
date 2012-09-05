New and Noteworthy
==================

0.7
~~~

Improved mocking failure messages
---------------------------------

The diagnostic message for "too many invocations" and "too few invocations" have been greatly improved. Here are two
examples::

    Too many invocations for:

    3 * person.sing(_)   (4 invocations)

    Matching invocations (ordered by last occurrence):

    2 * person.sing("do")   <-- this triggered the error
    1 * person.sing("re")
    1 * person.sing("mi")

And::

    Too few invocations for:

    1 * person.sing("fa")   (0 invocations)

    Unmatched invocations (ordered by similarity):

    1 * person.sing("re")
    1 * person.say("fa")
    1 * person2.shout("mi")

Stubs and spies
---------------

There are two new kinds of mock objects: stubs and spys. Stubs are created with the ``MockingApi.Stub()`` factory method.
(``MockingApi`` is a new superclass of ``spock.lang.Specification``.) They only provide behavior (typically by returning values)
but no verification. Consequently, stub interactions cannot have a cardinality (like ``1 * ...``).
By using a stub rather than a mock, the intention of the code becomes clearer.

Spys are created with the ``MockingApi.Spy()`` method. Like mocks, they can provide behavior and verification.
Unlike mocks, they default to delegating all invocations they receive to a real object of the same type. That object
is constructed as part of creating the spy, which is why the ``Spy()`` factory method accepts a list of constructor arguments::

    // underlying object is constructed with new Person("Fred", 33)
    def person = Spy(Person, constructorArgs: ["Fred", 33])

Spies can only be constructed for class types, but not for interface types.

Specifying interactions at construction time
--------------------------------------------

Interactions can now be specified at mock construction time::

    def person = Mock(Person) {
        person.sing() >> "tra-la-la
        3 * person.eat() // per day
    }

This feature is supported for all kinds of mock objects, but is especially handy for stubs.

Groovy mocks
------------

Spock now offers special mocking features for testing Groovy code. A Groovy mock is created with one of
the ``MockingApi.GroovyMock()``, ``MockingApi.GroovyStub()``, or ``MockingApi.GroovySpy()`` factory methods.
It automatically implements the ``groovy.lang.GroovyObject`` interface, and supports mocking of dynamic methods
using the same syntax as for declared methods::

    def list = GroovyStub(List)  // every time someone stubs a list, a kitten dies
    list.collect(_) >> [1, 2, 3] // stub Groovy's List.collect method

Global mocks
------------

A Groovy mock can be made _global_ by passing a ``global: true`` named
parameter to one of the aforementioned factory methods. With this option set, constructors and static methods can be mocked. For example::

    GroovySpy(Person, global: true) // no need to hold on to the mock object here
    1 * new Person("Fred") // expect constructor to get called once with "Fred" argument

    GroovyMock(ServiceFactory, global: true) // no need to hold on to the mock object here
    ServiceFactory.create() >> new FakeService() // return FakeService from static factory method

Apart from constructors and static methods, global mocks also intercept all invocations of instance methods on the mocked type.
Unlike regular mocks, they don't have to be injected into the code under specification. Instead, the code continues to use real objects,
whose behavior can be observed and controlled via the global mock::

    def anyPerson = GroovySpy(Person, global: true) // mock object is only needed in interactions
    anyPerson.sing() >> "tra-la-la" // stubs sing() method for all instances of Person

Global mocks can only be created for class types, but not for interface types.

Multiple conditions/interactions on the same target object
-------------------------------------------------------------

The ``Specification`` class now offers a ``with`` method. Similar in nature to Groovy's `Object.with()` method,
it sets a common target for a series of conditions::

    def person = new Person(name: "Fred", age: 33)

    expect:
    with(person) {
        name == "Fred"
        age == 33
        sex == "male"
    }

Likewise, the ``with`` method can be used for specifying _interactions_ on a common target::

    def service = Mock(Service)

    when:
    app.run()

    then:
    with(service) {
        1 * start()
        1 * act()
        1 * stop()
    }

0.6
~~~

Mocking improvements
--------------------

The mocking framework now provides better diagnostic messages in some cases.

Multiple result declarations can be chained. The following causes method bar to throw an ``IOException`` when first called, return the numbers one, two, and three on the next calls, and throw a ``RuntimeException`` for all subsequent calls::

    foo.bar() >> { throw new IOException() } >>> [1, 2, 3] >> { throw new RuntimeException() }

It's now possible to match any argument list (including the empty list) with ``foo.bar(*_)``.

Method arguments can now be constrained with `Hamcrest <http://code.google.com/p/hamcrest/>`_ matchers::

    import static spock.util.matcher.HamcrestMatchers.closeTo

    ...

    1 * foo.bar(closeTo(42, 0.001))

Extended JUnit rules support
----------------------------

In addition to rules implementing ``org.junit.rules.MethodRule`` (which has been deprecated in JUnit 4.9), Spock now also supports rules implementing the new ``org.junit.rules.TestRule`` interface. Also supported is the new ``@ClassRule`` annotation. Rule declarations are now verified and can leave off the initialization part. I that case Spock will automatically initialize the rule by calling the default constructor.
The ``@TestName`` rule, and rules in general, now honor the ``@Unroll`` annotation and any defined naming pattern.
 
See `Issue 240 <http://issues.spockframework.org/detail?id=240>`_ for a known limitation with Spock's TestRule support.

Condition rendering improvements
--------------------------------

When two objects are compared with the ``==`` operator, they are unequal, but their string representations are the same, Spock will now print the objects' types::

    enteredNumber == 42
    |             |
    |             false
    42 (java.lang.String)

JUnit fixture annotations
-------------------------

Fixture methods can now be declared with JUnit's ``@Before``, ``@After``, ``@BeforeClass``, and ``@AfterClass`` annotations, as an addition or alternative to Spock's own fixture methods. This was particularly needed for Grails 2.0 support.

Tapestry 5.3 support
--------------------

Thanks to a contribution from `Howard Lewis Ship <http://howardlewisship.com/>`_, the Tapestry module is now compatible with Tapestry 5.3. Older 5.x versions are still supported.

IBM JDK support
---------------

Spock now runs fine on IBM JDKs, working around a bug in the IBM JDK's verifier.

Improved JUnit compatibility
----------------------------

``org.junit.internal.AssumptionViolatedException`` is now recognized and handled as known from JUnit. ``@Unrolled`` methods no longer cause "yellow" nodes in IDEs.

.. _improved-unroll-0.6:

Improved ``@Unroll``
--------------------

The ``@Unroll`` naming pattern can now be provided in the method name, instead of as an argument to the annotation::

    @Unroll
    def "maximum of #a and #b is #c"() {
        expect:
        Math.max(a, b) == c

        where:
        a | b | c
        1 | 2 | 2
    }

The naming pattern now supports property access and zero-arg method calls::

    @Unroll
    def "#person.name.toUpperCase() is #person.age years old"() { ... }

The ``@Unroll`` annotation can now be applied to a spec class. In this case, all data-driven feature methods in the class will be unrolled.

Improved ``@Timeout``
---------------------

The ``@Timeout`` annotation can now be applied to a spec class. In this case, the timeout applies to all feature methods (individually) that aren't already annotated with ``@Timeout``.
Timed methods are now executed on the regular test framework thread. This can be important for tests that rely on thread-local state (like Grails integration tests). Also the interruption behavior has been improved, to increase the chance that a timeout can be enforced.

The failure exception that is thrown when a timeout occurs now contains the stacktrace of test execution, allowing you to see where the test was “stuck” or how far it got in the allocated time.

Improved data table syntax
--------------------------

Table cells can now be separated with double pipes. This can be used to visually set apart expected outputs from provided inputs::

    ...
    where:
    a | b || sum
    1 | 2 || 3
    3 | 1 || 4

Groovy 1.8/2.0 support
----------------------

Spock 0.6 ships in three variants for Groovy 1.7, 1.8, and 2.0. Make sure to pick the right version - for example, for Groovy 1.8 you need to use spock-core-0.6-groovy-1.8 (likewise for all other modules). The Groovy 2.0 variant is based on Groovy 2.0-beta-3-SNAPSHOT and only available from http://m2repo.spockframework.org. The Groovy 1.7 and 1.8 variants are also available from Maven Central. The next version of Spock will no longer support Groovy 1.7.

Grails 2.0 support
------------------

Spock's Grails plugin was split off into a separate project and now lives at http://github.spockframework.org/spock-grails. The plugin supports both Grails 1.3 and 2.0.

The Spock Grails plugin supports all of the new Grails 2.0 test mixins, effectively deprecating the existing unit testing classes (e.g. UnitSpec). For integration testing, IntegrationSpec must still be used.

IntelliJ IDEA integration
-------------------------

The folks from `JetBrains <http://www.jetbrains.com>`_ have added a few handy features around data tables. Data tables will now be layed out automatically when reformatting code. Data variables are no longer shown as "unknown" and have their types inferred from the values in the table (!).

GitHub repository
-----------------

All source code has moved to http://github.spockframework.org/. The `Grails Spock plugin <http://github.spockframework.org/spock-grails>`_, `Spock Example <http://github.spockframework.org/spock-example>`_ project, and `Spock Web Console <http://github.spockframework.org/spockwebconsole>`_ now have their own GitHub projects. Also available are slides and code for various Spock presentations (like `this one <http://github.spockframework.org/smarter-testing-with-spock>`_).

Gradle build
------------

Spock is now exclusively built with Gradle. Building Spock yourself is as easy as cloning the `GitHub repo <http://github.spockframework.org/spock>`_ and executing ``gradlew build``. No build tool installation is required; the only prerequisite for building Spock is a JDK installation (1.5 or higher).

Fixed Issues
------------

See the `issue tracker <http://issues.spockframework.org/list?can=1&q=label%3AMilestone-0.6>`_ for a list of fixed issues.

