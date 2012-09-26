New and Noteworthy
==================

0.7
~~~

Improved mocking failure message for "too many invocations"
-----------------------------------------------------------

The diagnostic message accompanying a "too many invocations" failure has been greatly improved.
Here is an example::

    Too many invocations for:

    3 * person.sing(_)   (4 invocations)

    Matching invocations (ordered by last occurrence):

    2 * person.sing("do")   <-- this triggered the error
    1 * person.sing("re")
    1 * person.sing("mi")

Improved mocking failure message for "too few invocations"
----------------------------------------------------------

The diagnostic message accompanying a "too few invocations" failure has been greatly improved.
Here is an example::

    Too few invocations for:

    1 * person.sing("fa")   (0 invocations)

    Unmatched invocations (ordered by similarity):

    1 * person.sing("re")
    1 * person.say("fa")
    1 * person2.shout("mi")

Stubs
-----

Besides mocks, Spock now has explicit support for stubs::

    def stub = Stub(Person)

A stub is a restricted form of mock object that responds to invocations without ever demanding them.
Other than not having a cardinality, the syntax for stub interactions is exactly the same as for mock interactions.
Using a stub over a mock is an effective way to communicate its role to readers of the specification.

Read more in the :ref:`documentation <Stubs>`.

Spies
-----

Besides mocks, Spock now has support for spies::

    def spy = Spy(Person, constructorArgs: ["Fred"])

A spy sits atop a real object, in this example an instance of class ``Person``. All invocations on the spy
that don't match an interaction are delegated to that object. This allows to listen in on and selectively
change the behavior of the real object. Furthermore, spies can be used as partial mocks.

Read more in the :ref:`documentation <Spies>`.

Specifying interactions at mock creation time
---------------------------------------------

Interactions can now be specified at mock creation time::

    def person = Mock(Person) {
        person.sing() >> "tra-la-la
        3 * person.eat()
    }

Groovy mocks
------------

Spock now offers specialized mock objects for spec'ing Groovy code::

    def mock = GroovyMock(Person)
    def stub = GroovyStub(Person)
    def spy = GroovySpy(Person)

A Groovy mock automatically implements ``groovy.lang.GroovyObject``. It allows stubbing and mocking
of dynamic methods just like for statically declared methods. When a Groovy mock is called from Java
rather than Groovy code, it behaves like a regular mock.

Read more in the :ref:`documentation <GroovyMocks>`.

Global mocks
------------

A Groovy mock can be made *global*::

    GroovySpy(Person, global: true)

A global mock effectively replaces all instances of its type and makes them amenable to stubbing and mocking.
Furthermore, it allows mocking of the type's constructors and static methods.

Read more in the :ref:`documentation <GlobalMocking>`.

Shortcut for conditions involving the same target object
--------------------------------------------------------

Similar in nature to Groovy's `Object.with()` method, the ``Specification.with()`` avoids the need to repeat
the target object in a series of conditions::

    def person = new Person(name: "Fred", age: 33)

    expect:
    with(person) {
        name == "Fred"
        age == 33
        sex == "male"
    }

Shortcut for interactions involving the same mock object
--------------------------------------------------------

The ``with`` method can also be used to set a common target for interactions::

    def service = Mock(Service)
    app.service = service

    when:
    app.run()

    then:
    with(service) {
        1 * start()
        1 * act()
        1 * stop()
    }

Polling conditions
------------------

``spock.util.concurrent.PollingConditions` joins `AsyncConditions` and `BlockingVariable(s)` as another utility for
dealing with asynchronous events::

    def person = new Person(name: "Fred", age: 22)
    def conditions = new PollingConditions(timeout: 10)

    when:
    Thread.start {
        sleep(1000)
        person.age == 42
        sleep(5000)
        person.name == "Barney"
    }

    then:
    conditions.within(3, SECONDS) {
        assert person.age == 42
    }

    conditions.eventually {
        assert person.name == "Fred"
    }

Splitting up class Specification
--------------------------------

Parts of class ``spock.lang.Specification`` were pulled up into two new super classes: ``spock.lang.MockingApi``
now contains all mocking-related methods, and ``org.spockframework.lang.SpecInternals`` contains internal methods
which aren't meant to be used directly.

Improved failure messages for notThrown() and noExceptionThrown()
-----------------------------------------------------------------

Instead of just passing through exceptions, ``Specification.notThrown()`` and ``Specification.noExceptionThrown()``
now fail with messages like::

    Expected no exception to be thrown, but got 'java.io.FileNotFoundException'

    Caused by: java.io.FileNotFoundException: ...

HamcrestSupport.expect()
------------------------

Class ``spock.util.matcher.HamcrestSupport`` got an ``expect`` method that makes
[Hamcrest](http://code.google.com/p/hamcrest/) assertions read better in then-blocks::

    when:
    def x = computeValue()

    then:
    expect x, closeTo(42, 0.01)

@Beta
-----

Recently introduced classes and methods may be annotated with @Beta, as a sign that they may still undergo incompatible
changes. This gives us a chance to incorporate valuable feedback from our users. (Yes, we need your feedback!) Typically,
a @Beta annotation is removed within one or two releases.

Fixed issues
------------

See the `issue tracker <http://issues.spockframework.org/list?can=1&q=label%3AMilestone-0.7>`_ for a list of fixed issues.

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

Fixed issues
------------

See the `issue tracker <http://issues.spockframework.org/list?can=1&q=label%3AMilestone-0.6>`_ for a list of fixed issues.

