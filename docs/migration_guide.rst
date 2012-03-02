Migration Guide
===============

This page explains incompatible changes between successive versions and provides suggestions on how to deal with them.

Version 0.6
-----------

Class initialization order
~~~~~~~~~~~~~~~~~~~~~~~~~~

    .. note:: This only affects cases where one specification class inherits from another one.

Given these specifications::

    class Base extends Specification {
        def base1 = "base1"
        def base2

        def setup() { base2 = "base2" }
    }

    class Derived extends Base {
        def derived1 = "derived1"
        def derived2

        def setup() { derived2 = "derived2" }
    }

In 0.5, above assignments happened in the order ``base1``, ``base2``, ``derived1``, ``derived2``. In other words, field initializers were executed right before the setup method in the same class. In 0.6, assignments happen in the order ``base1``, ``derived1``, ``base2``, ``derived2``. This is a more conventional order that solves a few problems that users faced with the previous behavior, and also allows us to support JUnit's new ``TestRule``. As a result of this change, the following will no longer work::

    class Base extends Specification {
        def base

        def setup() { base = "base" }
    }

    class Derived extends Base {
        def derived = base + "derived" // base is not yet set
    }

To overcome this problem, you can either use a field initializer for ``base``, or move the assignment of ``derived`` into a setup method.

``@Unroll`` naming pattern syntax
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    .. note:: This is not a change from 0.5, but a change compared to 0.6-SNAPSHOT.

    .. note:: This only affects the Groovy 1.8 and 2.0 variants.

In 0.5, the naming pattern was string based::

    @Unroll("maximum of #a and #b is #c")
    def "maximum of two numbers"() {
        expect:
        Math.max(a, b) == c

        where:
        a | b | c
        1 | 2 | 2
    }

In 0.6-SNAPSHOT, this was changed to a closure returning a ``GString``::

    @Unroll({"maximum of $a and $b is $c"})
    def "maximum of two numbers"() { ... }

For various reasons, the new syntax didn't work out as we had hoped, and eventually we decided to go back to the string based syntax. See :ref:`improved-unroll-0.6` for recent improvements to that syntax.

Hamcrest matcher syntax
~~~~~~~~~~~~~~~~~~~~~~~

    .. note:: This only affects users moving from the Groovy 1.7 to the 1.8 or 2.0 variant.

Spock offers a very neat syntax for using `Hamcrest <http://code.google.com/p/hamcrest/>`_ matchers::

    import static spock.util.matcher.HamcrestMatchers.closeTo

    ...

    expect:
    answer closeTo(42, 0.001)

Due to changes made between Groovy 1.7 and 1.8, this syntax no longer works in as many cases as it did before. For example, the
following will no longer work::

    expect:
    object.getAnswer() closeTo(42, 0.001)

To avoid such problems, use ``HamcrestSupport.that``::

    import static spock.util.matcher.HamcrestSupport.that

    ...

    expect:
    that answer, closeTo(42, 0.001)

A future version of Spock will likely remove the former syntax and strengthen the latter one.









