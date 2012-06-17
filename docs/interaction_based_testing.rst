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

The Java world has no shortage of popular and mature mocking frameworks: JMock, EasyMock, Mockito, to name just a few.
Although any of these frameworks can be used together with Spock, we didn’t stop here. Instead, Spock comes with
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

Mocking is the act of describing expected interactions, and failing the test if they don't happen. Let's see an example::

    def "should send messages to all subscribers"() {
        when:
        publisher.send("hello")

        then:
        1 * subscriber1.receive("hello")
        1 * subscriber2.receive("hello")
    }

Read out aloud: "When the publisher sends a 'hello' message, then both subscribers should receive the message exactly once."

When this test gets run, Spock watches all invocations on mock objects that happen during the execution of the
``when`` block and compares them to the expected interactions described in the corresponding ``then`` block. In case of
a mismatch, a (subclass of) ``InteractionNotSatisfiedError`` is thrown.

Interactions
~~~~~~~~~~~~

Let's take a closer look at the ``then`` block. It contains two *interactions*, each of which consists of four
parts: a *cardinality*, a *target constraint*, a *method constraint*, and an *argument list constraint*:

.. sidebar:: Is an interaction just a regular method invocation?

    Not quite. While an interaction looks similar to a regular method invocation, it is simply a way to express which
    method invocations are expected to happen. A good way to think of an interaction is as a regular expression
    that is matched against all invocations on mock objects. Depending on the circumstances, the interaction may match
    zero, one, or multiple invocations.

.. code-block::

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
                                     // (starts with 'r', ends in 'e')

Argument List Constraint
~~~~~~~~~~~~~~~~~~~~~~~~

The argument list constraint of an interaction tells which method arguments are expected::

    1 * subscriber1.receive("hello")     // an argument that is equal[#equality]_ to the String "hello"
    1 * subscriber1.receive(!"hello")    // an argument that is unequal[#equality]_ to the String "hello"
    1 * subscriber1.receive(_)           // any single argument (including null)
    1 * subscriber1.receive(!null)       // any non-null argument
    1 * subscriber1.receive(_ as String) // any non-null argument that is-a String
    1 * subscriber1.receive(*_)          // any argument list (including the empty argument list)

Stubbing
--------

.. rubric:: Footnotes

.. [#equality] Arguments are compared according to Groovy equality, which is somewhat more relaxed than Java equality (in particular for numbers).