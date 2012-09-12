Interaction Based Testing
=========================

Interaction-based testing is a testing and design technique that emerged in the Extreme Programming
(XP) community in the early 2000's. Focusing on the behavior of objects rather than their state, it explores how
the object(s) under test interact, by way of method calls, with their collaborators.

For example, suppose we have a ``Publisher`` that sends messages to its ``Subscriber``'s::

    class Publisher {
        List<Subscriber> subscribers
        void send(String message)
    }

    interface Subscriber {
        void receive(String message)
    }

    class PublisherSpec extends Specification {
        Publisher publisher = new Publisher()
    }

How are we going to test ``Publisher``? With state-based testing we can easily verify that the publisher correctly
manages its list of subscribers. The more interesting question, though, is whether a message sent by the publisher
is received by all registered subscribers. To answer this question, we need a special implementation of
``Subscriber`` that listens in on the conversation between the publisher and its subscribers. Such an
implementation is often called a ``mock object``.

While we could certainly create a mock implementation of ``Subscriber`` by hand, writing and maintaining this code
can get unpleasant as the number of methods and complexity of interactions increases. This is where mocking frameworks
come in. They allow to succinctly describe the expected interactions between the object under test and its
collaborators. Mock implementations are synthesized on the fly based on this description.

The Java world has no shortage of popular and mature mocking frameworks: `JMock <http://www.jmock.org/>`_,
`EasyMock <http://www.easymock.org/>`_, `Mockito <http://code.google.com/p/mockito/>`_, to name just a few.
Although each of these frameworks can be used together with Spock, we didn’t stop here. Instead, Spock comes with
its own mocking framework that leverages the power of Groovy to make interaction-based tests easier to write,
more readable, and ultimately more fun.

.. note:: All features of Spock's mocking framework can be used to, and work the same for, testing both Java and Groovy code.

Creating Mock Objects
---------------------

Mock objects are created with the ``Specification.Mock()`` method. Let's create two mock subscribers::

    def subscriber1 = Mock(Subscriber)
    def subscriber2 = Mock(Subscriber)

Alternatively, the following Java-like syntax is supported, which typically results in better IDE support::

    Subscriber subscriber1 = Mock()
    Subscriber subscriber2 = Mock()

.. note:: If the type of the mock is given on the left-hand side, it does not have to be repeated on the right-hand side.

Mock objects literally implement (or, in the case of a class, extend) the type they stand in for. (In other words, in
our example ``subscriber1`` *is-a* ``Subscriber``.) Hence they can be passed to statically typed (Java) code that expects
such a type.

Default Behavior of Mock Objects
--------------------------------

.. sidebar:: Lenient vs. strict mocking frameworks

    Like Mockito, we firmly believe that a mocking framework should be lenient by default. This means that unexpected
    method calls on mock objects (or, in other words, interactions that aren't relevant for the test at hand) are allowed
    and will simply return a default value. Conversely, mocking frameworks like EasyMock and JMock are strict by default;
    they will throw an exception for every unexpected method call. While strictness enforces high rigor, it can also lead
    to over-specification, resulting in brittle tests that fail with every other internal code change. Spock's mocking
    framework makes it easy to describe only what's relevant about an interaction, avoiding the over-specification trap.

Initially, mock objects have no behavior; calling their methods is allowed but will have no effect other than returning
the default value for the method's return type (``false``, ``0``, or ``null``). An exception are the ``java.lang.Object``
methods ``equals``, ``hashCode``, and ``toString``, which have the following default behavior: A mock object is only
equal to itself, has a unique hash code, and a string representation that includes the name of the type it represents.
This default behavior is overridable.

Registering Mock Objects with Object Under Test
-----------------------------------------------

After creating the publisher and subscribers, we need to register the latter with the former::

    class PublisherSpec extends Specification {
        Publisher publisher = new Publisher()
        Subscriber subscriber1 = Mock()
        Subscriber subscriber2 = Mock()

        def setup() {
            publisher << subscriber1 // Groovy shorthand for List.add()
            publisher << subscriber2
        }
    }

Now we are ready to describe the expected interactions between the two parties.

Mocking
-------

Mocking is the act of describing expected interactions and verifying them against actual invocations. Let's see an example::

    def "should send messages to all subscribers"() {
        when:
        publisher.send("hello")

        then:
        1 * subscriber1.receive("hello")
        1 * subscriber2.receive("hello")
    }

Read out aloud: "When the publisher sends a 'hello' message, then both subscribers should receive that message exactly once."

When this test gets run, Spock watches all invocations on mock objects that occur during the execution of the
``when`` block and compares them to the expected interactions described in the corresponding ``then`` block. In case of
a mismatch, a (subclass of) ``InteractionNotSatisfiedError`` is thrown. This verification happens automatically and
does not require any boilerplate code as often seen with other mocking frameworks.

Interactions
~~~~~~~~~~~~

.. sidebar:: Is an interaction just a regular method invocation?

    Not quite. While an interaction looks similar to a regular method invocation, it is simply a way to express which
    method invocations are expected to happen. A good way to think of an interaction is as a regular expression
    that is matched against all invocations on mock objects. Depending on the circumstances, the interaction may match
    zero, one, or multiple invocations.

Let's take a closer look at the ``then`` block. It contains two *interactions*, each of which consists of four
parts: a *cardinality*, a *target constraint*, a *method constraint*, and one ore more *argument constraints*::

    1 * subscriber1.receive("hello")
    |   |           |       |
    |   target c.   |       argument list constraint
    cardinality     method constraint

Cardinality
~~~~~~~~~~~

The cardinality of an interaction tells how often a method call is expected. It can either be a fixed number or
a range::

    1 * subscriber1.receive("hello")      // exactly one call
    0 * subscriber1.receive("hello")      // zero calls
    (1..3) * subscriber1.receive("hello") // between one and three calls (inclusive)
    (1.._) * subscriber1.receive("hello") // at least one call
    (_..3) * subscriber1.receive("hello") // at most three calls
    _ * subscriber1.receive("hello")      // any number of calls, including zero (allowed but rarely needed)

Target Constraint
~~~~~~~~~~~~~~~~~

The target constraint of an interaction tells which mock object a call is expected on::

  1 * subscriber1.receive("hello") // a call on 'subscriber1'
  1 * _.receive("hello")           // a call on any mock object

Method Constraint
~~~~~~~~~~~~~~~~~

The method constraint of an interaction tells which method is expected to be called::

    1 * subscriber1.receive("hello") // a method named 'receive'
    1 * subscriber1./r.*e/("hello")  // a method whose name matches the given regular expression
                                     // (here: method name starts with 'r', ends in 'e')

Argument Constraints
~~~~~~~~~~~~~~~~~~~~

The argument constraints of an interaction tell which method arguments are expected::

    1 * subscriber1.receive("hello")     // an argument that is equal[#equality]_ to the String "hello"
    1 * subscriber1.receive(!"hello")    // an argument that is unequal[#equality]_ to the String "hello"
    1 * subscriber1.receive(_)           // any single argument (including null)
    1 * subscriber1.receive(!null)       // any non-null argument
    1 * subscriber1.receive(_ as String) // any non-null argument that is-a String
    1 * subscriber1.receive(*_)          // any argument list (including the empty argument list)
    1 * subscriber1.receive({ it.size() > 3 }) // an argument that satisfies the given predicate
                                               // (here: message length is greater than 3)

Argument constraints work as expected for methods with multiple arguments and/or varargs::

    1 * process.invoke("ls", "-a", _, !null, { ["abcdefghiklmnopqrstuwx1"].contains(it) })

.. admonition:: Spock Deep Dive

    Groovy allows any method whose last parameter has an array type to be called in vararg style. Consequently,
    vararg syntax is also allowed in interactions describing invocations of such methods.

Verification of Interactions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There a two main ways in which a mock-based test can fail: An interaction can match more invocations than
allowed, or it can match fewer invocations than required. The former case is detected right when the invocation
happens, and results in a ``TooManyInvocationsError``::

    Too many invocations for:

    1 * subscriber.receive("hello") (2 invocations)

The second case (fewer invocations than required) can only be detected once execution of the ``when`` block has completed.
(Until then, further invocations may occur.) It results in a ``TooFewInvocationsError``::

    Too few invocations for:

    1 * subscriber1.receive("hello") (0 invocations)

Note that it doesn't matter whether the method was not called at all, called on another mock object, or called with
a different argument; in either case, the same error will occur.

.. admonition:: New in Spock 0.7: Show Unmatched Invocations

    To make it easier to diagnose what happened "instead" of a missing invocation, Spock will show all
    invocations that didn't match any interaction. This is particularly helpful when a method invocation has the "wrong"
    arguments::

        Unmatched invocations:

        subscriber1.receive('olleh')

Invocation Order
~~~~~~~~~~~~~~~~

Often, the exact method invocation order isn't relevant and may change over time. To avoid over-specification,
Spock defaults to allowing any invocation order, provided that the specified interactions are eventually satisfied::

    then:
    2 * foo.bar()
    1 * foo.baz()

Here, any of the invocation sequences ``foo.bar(); foo.bar(); foo.baz()``, ``foo.bar(); foo.baz();
foo.bar()`` and ``foo.baz(); foo.bar(); foo.bar()`` will satisfy the specified interactions.

In those cases where invocation order matters, you can impose an order by splitting up interactions into
multiple then-blocks::

    then:
    1 * foo.baz()

    then:
    2 * foo.bar()

Here, Spock will verify that the invocation of ``baz`` happen before any invocation of ``bar``.
In other words, invocation order is enforced *between* then-blocks, but not *within* a then-block.

Mocking Classes
~~~~~~~~~~~~~~~

In addition to interfaces, Spock also supports mocking of classes. Mocking classes works
just like mocking interfaces; the only additional requirement is to put ``cglib-nodep-2.2`` or higher
and ``objenesis-1.2`` or higher on the class path. If either of these libraries is missing from
the class path, Spock will gently let you know.

Stubbing
--------

Further Reading
---------------

To learn more about interaction-based testing, we recommend the following resources:

* `Endo-Testing: Unit Testing with Mock Objects <http://connextra.com/aboutUs/mockobjects.pdf>`_

  Paper from the XP2000 conference that introduces the concept of mock objects.

* `Mock Roles, not Objects <http://www.jmock.org/oopsla2004.pdf>`_

  Paper from the OOPSLA2004 conference that explains how to do mocking *right*.

* `Mocks Aren't Stubs <http://martinfowler.com/articles/mocksArentStubs.html>`_

  Martin Fowler's take on mocking.

* `Growing Object-Oriented Software Guided by Tests <http://www.growing-object-oriented-software.com/>`_

  TDD pioneers Steve Freeman and Nat Pryce explain in detail how test-driven development and mocking work in the real world.

.. rubric:: Footnotes

.. [#equality] Arguments are compared according to Groovy equality, which is based on, but more relaxed than, Java equality (in particular for numbers).