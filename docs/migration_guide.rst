Migration Guide
===============

Breaking changes from 0.5
-------------------------

Initialization order when inheriting from another spec class
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Previously, Spock executed field/property initializers just before executing the setup method in the same class. When inheriting from another spec, this meant that the base classes' setup method would get executed before the derived classes' field initializers. This caused problems when a setup method in a base class called an abstract method overridden in a derived class. In that case, the derived classes' fields were not yet initialized. In 0.6, Spock uses a more traditional model where first all fields are initialized (starting from the base class) and then all setup methods are executed (again starting from the base class). If you have a case where a field initializer in a derived class depends on the setup method of the base class, you can either move the derived classes' field initializer into the setup method, or move the relevant part of the base classes' setup method into a field initializer.

Breaking changes from 0.6-SNAPSHOT
----------------------------------

``@Unroll`` naming pattern syntax
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    .. note:: This only affects the Groovy 1.8 and 2.0 variants.

In 0.5, the naming pattern was string-based::

    @Unroll("maximum of #a and #b is #c")
    def "maximum of two numbers"() {
        expect:
        Math.max(a, b) == c

        where:
        a | b | c
        1 | 2 | 2
    }

In 0.6-SNAPSHOT, this was changed to a closure/GString-based pattern::

    @Unroll({"maximum of $a and $b is $c"})
    def "maximum of two numbers"() { ... }

For various reasons, this didn't play out as we had hoped, and eventually we decided to go back to the string-based syntax. See :ref:`improved-unroll-0.6` for recent improvements made to that syntax.

