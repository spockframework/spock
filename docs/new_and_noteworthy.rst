New and Noteworthy
==================

Version 0.6
~~~~~~~~~~~

Mocking improvements
--------------------

The mocking framework now provides better diagnostic messages in some cases.

Multiple result declarations can be chained. The following causes method bar to throw an ``IOException`` when first called, return the numbers one, two, and three on the next calls, and throw a ``RuntimeException`` for all subsequent calls::

    foo.bar() >> { throw new IOException() } >>> [1, 2, 3] >> { throw new RuntimeException() }

It's now possible to match any argument list (including the empty list). The syntax for this is ``foo.bar(*_)``.

Extended support for JUnit rules
--------------------------------

In addition to rules implementing ``org.junit.rules.MethodRule`` (which has been deprecated in JUnit 4.9), Spock now also supports rules implementing the new ``org.junit.rules.TestRule`` interface. Also supported is the new ``@ClassRule`` annotation. Rule declarations are now verified and can leave off the initialization part. I that case Spock will automatically initialize the rule by calling the default constructor.
The ``@TestName`` rule, and rules in general, now honor the ``@Unroll`` annotation and any defined naming pattern.
 
See `Issue 240 <http://issues.spockframework.org/detail?id=240>`_ for a known limitation in Spock's TestRule support.

Improvements to how a failed condition is rendered
--------------------------------------------------

When two objects that are compared with the ``==`` operator are unequal but their string representations are the same, Spock will now print the objects' types::

    enteredNumber == 42
    |             |
    |             false
    42 (java.lang.String)

Support for JUnit's ``@Before``, ``@After``, ``@BeforeClass``, and ``@AfterClass`` annotations
----------------------------------------------------------------------------------------------

JUnit-style fixture methods can now be declared in any spec, as an addition or alternative to Spock's own fixture methods. This was particularly needed for Grails 2.0 support.

Support for Tapestry 5.3
------------------------

Thanks to a contribution from `Howard Lewis Ship <http://howardlewisship.com/>`_, the Tapestry module is now compatible with Tapestry 5.3. Older 5.x versions are still supported.

Support for IBM JDKs
--------------------

Spock now runs fine on IBM JDKs, working around a bug in the IBM JDK's verifier.

Further improved JUnit compatibility
------------------------------------

``org.junit.internal.AssumptionViolatedException`` is now recognized and handled as known from JUnit. ``@Unrolled`` methods no longer cause "yellow" nodes in IDEs.

.. _improved-unroll-0.6:

Improvements to ``@Unroll``
---------------------------

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

Improvements to ``@Timeout``
----------------------------

The ``@Timeout`` annotation can now be applied to a spec class. In this case, the timeout applies to all feature methods (individually) that aren't already annotated with ``@Timeout``.
Timed methods are now executed on the regular test framework thread. This can be important for tests that rely on thread-local state (like Grails integration tests). Also the interruption behavior has been improved, to increase the chance that a timeout can be enforced.

Improved data table syntax
--------------------------

Table cells can now be separated with double pipes. This can be used to visually set apart expected outputs from provided inputs::

    ...
    where:
    a | b || sum
    1 | 2 || 3
    3 | 1 || 4

Support for Groovy 1.8 and Groovy 2.0
-------------------------------------

Spock 0.6 ships in three variants for Groovy 1.7, 1.8, and 2.0. Make sure to pick the right version - for example, for Groovy 1.8 you need to use spock-core-0.6-groovy-1.8 (likewise for all other modules). The Groovy 2.0 variant is based on Groovy 2.0-beta-3-SNAPSHOT and only available from http://m2repo.spockframework.org. The Groovy 1.7 and 1.8 variants are also available from Maven Central. The next version of Spock will no longer support Groovy 1.7.

Support for Grails 2.0
----------------------

Spock's Grails plugin was split off into a separate project and now lives at http://github.spockframework.org/spock-grails. The plugin supports both Grails 1.3 and 2.0.

Source code repository moved to GitHub
--------------------------------------

All source code has moved to http://github.spockframework.org/. The `Grails Spock plugin <http://github.spockframework.org/spock-grails>`_, `Spock Example <http://github.spockframework.org/spock-example>`_ project, and `Spock Web Console <http://github.spockframework.org/spockwebconsole>`_ are now their own GitHub projects. Also available are slides and code for various Spock presentations (like `this one <http://github.spockframework.org/smarter-testing-with-spock>`_).

Gradle build
------------

Spock is now exclusively built with Gradle. Building Spock yourself is as easy as cloning the `GitHub repo <http://github.spockframework.org/spock>`_ and executing ``gradlew build``. No build tool installation is required; the only prerequisite for building Spock is a JDK installation (1.5 or higher).

