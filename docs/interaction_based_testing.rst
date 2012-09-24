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

How are we going to test ``Publisher``? With state-based testing, we can easily verify that the publisher correctly
manages its list of subscribers. The more interesting question, though, is whether a message sent by the publisher
is received by all registered subscribers. To answer this question, we need a special implementation of
``Subscriber`` that listens in on the conversation between the publisher and its subscribers. Such an
implementation is often called a *mock object*.

While we could certainly create a mock implementation of ``Subscriber`` by hand, writing and maintaining this code
can get unpleasant as the number of methods and complexity of interactions increases. This is where mocking frameworks
come in. They allow to succinctly describe the expected interactions between the object under test and its
collaborators. Mock implementations are synthesized on the fly based on this description.

The Java world has no shortage of popular and mature mocking frameworks: `JMock <http://www.jmock.org/>`_,
`EasyMock <http://www.easymock.org/>`_, `Mockito <http://code.google.com/p/mockito/>`_, to name just a few.
Although each of these frameworks can be used together with Spock, we didn’t stop here. Instead, Spock comes with
its own mocking framework that leverages the power of Groovy to make interaction-based tests easier to write,
more readable, and ultimately more fun.

.. note:: Except where indicated, all features of Spock's mocking framework work both for testing Java and Groovy code.

Creating Mock Objects
---------------------

Mock objects are created with the ``MockingApi.Mock()`` method.[#creating]_ Let's create two mock subscribers::

    def subscriber1 = Mock(Subscriber)
    def subscriber2 = Mock(Subscriber)

Alternatively, the following Java-like syntax is supported, which typically results in better IDE support::

    Subscriber subscriber1 = Mock()
    Subscriber subscriber2 = Mock()

.. note:: If the type of the mock is given on the left-hand side, it does not have to be repeated on the right-hand side.

Mock objects literally implement (or, in the case of a class, extend) the type they stand in for. (In
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

Where to put my Interactions?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

So far, we have put our interactions into a then-block. This often results in a spec that reads naturally.
However, it is also permissible to put interactions anywhere *before* the when-block that is supposed to trigger
them. In particular, this allows to put interactions into a ``setup`` method.

When an invocation on a mock object occurs, it is matched against interactions in their declared order.
Hence interactions declared earlier will win in case of an overlap. There is one exception to this rule:
Interactions declared in a then-block are matched before any other interactions. This allows to override interactions
declared in, say, a ``setup`` method with interactions declared in a then-block.

Explicit Interaction Blocks
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Like most other mocking frameworks, Spock must have full information about expected interactions
*before* they take place. So how is it possible for interactions to be declared in a then-block?
The answer is that Spock internally moves interactions declared in a then-block to immediately
before the preceding when-block.

Usually this works out just fine, but sometimes it can lead to problems::

    when:
    publisher.send("message")

    then:
    def message = "message"
    1 * subscriber.receive(message)

Here we have parameterized the expected argument. (Likewise, we could have parameterized the
invocation count.) However, Spock isn't smart enough (huh?) to tell that the interaction is intrinsically
linked to the preceding variable declaration. Hence it will just move the interaction, which
will blow up at runtime with a ``MissingPropertyException``.

There are two ways to remedy this situation: Move both lines of code before the when-block yourself,
or be explicit about them belonging together::

    when:
    publisher.send("message")

    then:
    interaction {
        def message = "message"
        1 * subscriber.receive(message)
    }

After giving it a hint by using the ``MockingApi.interaction`` method, Spock will do the right thing
and move both lines of code to immediately before the when-block. Problem solved!

Scope of Interactions
~~~~~~~~~~~~~~~~~~~~~

Interactions declared in a then-block are scoped to the preceding when-block::

    when:
    publisher.send("message1")

    then:
    subscriber1.receive("message1")

    when:
    publisher.send("message2")

    then:
    subscriber1.receive("message2")

This makes sure that ``subscriber1`` receives ``"message1"`` during execution of the first when-block,
and ``"message2"`` during execution of the second when-block.

Interactions declared outside a then-block are valid from their declaration until the end of the
containing feature method.

Interactions always occur in the context of a feature method. Hence they cannot be declared in a
``setupSpec`` or ``cleanupSpec`` method. Likewise, mock objects cannot be ``@Shared``.

Verification of Interactions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There a two main ways in which a mock-based test can fail: An interaction can match more invocations than
allowed, or it can match fewer invocations than required. The former case is detected right when the invocation
happens, and results in a ``TooManyInvocationsError``::

    Too many invocations for:

    2 * subscriber.receive(_) (3 invocations)

.. admonition:: New in Spock 0.7: Show All Matching Invocations

    To make it easier to diagnose why too many invocations matched, Spock will show all invocations matching
    the interaction in question::

        Matching invocations (ordered by last occurrence):

        2 * subscriber.receive("hello")   <-- this triggered the error
        1 * subscriber.receive("goodbye")

    According to this output, one of the ``receive("hello")`` calls triggered the ``TooManyInvocationsError``.
    Note that because indistinguishable calls like the two invocations of ``subscriber.receive("hello")`` are aggregated
    into a single line of output, the first ``receive("hello")`` may well have occurred before the ``receive("goodbye")``.

The second case (fewer invocations than required) can only be detected once execution of the ``when`` block has completed.
(Until then, further invocations may still occur.) It results in a ``TooFewInvocationsError``::

    Too few invocations for:

    1 * subscriber1.receive("hello") (0 invocations)

Note that it doesn't matter whether the method was not called at all, the same method was called with different arguments,
the same method was called on a different mock object, or a different method was called "instead" of this one;
in either case, the same ``TooFewInvocationsError`` error will occur.

.. admonition:: New in Spock 0.7: Show Unmatched Invocations

    To make it easier to diagnose what happened "instead" of a missing invocation, Spock will show all
    invocations that didn't match any interaction, ordered by their similarity with the interaction in question.
    In particular, invocations that match everything but the interaction's arguments will be shown first::

        Unmatched invocations (ordered by similarity):

        1 * subscriber1.receive("goodbye")
        1 * subscriber2.receive("hello")

Invocation Order
~~~~~~~~~~~~~~~~

Often, the exact method invocation order isn't relevant and may change over time. To avoid over-specification,
Spock defaults to allowing any invocation order, provided that the specified interactions are eventually satisfied::

    then:
    2 * subscriber.receive("hello")
    1 * subscriber.receive("goodbye")

Here, any of the invocation sequences ``hello hello goodbye``, ``hello goodbye hello``, and
``goodbye hello hello`` will satisfy the specified interactions.

In those cases where invocation order matters, you can impose an order by splitting up interactions into
multiple then-blocks::

    then:
    2 * subscriber.receive("hello")

    then:
    1 * subscriber.receive("goodbye")

Now Spock will verify that both ``hello``'s are received before the ``goodbye``.
In other words, invocation order is enforced *between* but not *within* then-blocks.

.. note:: Splitting up a then-block with ``and:`` does not impose any ordering, as ``and:``
          is only meant for documentation purposes and doesn't carry any semantics.

Mocking Classes
~~~~~~~~~~~~~~~

Besides interfaces, Spock also supports mocking of classes. Mocking classes works
just like mocking interfaces; the only additional requirement is to put ``cglib-nodep-2.2`` or higher
and ``objenesis-1.2`` or higher on the class path. If either of these libraries is missing from
the class path, Spock will gently let you know.

Stubbing
--------

Stubbing is the act of "programming" collaborators to exhibit a certain behavior. When stubbing
a method, you don't care if and how many times the method is going to be called; you just want it to
return some value (or perform some side effect) *whenever* it gets called.

For the sake of demonstrating stubbing, let's modify the ``Subscriber``'s ``receive`` method
to return a status code that tells if the subscriber was able to process the message::

    interface Subscriber {
        String receive(String message)
    }

Returning Fixed Values
~~~~~~~~~~~~~~~~~~~~~~

To return the same value every time ``receive`` gets called, use the right-shift (``>>``) operator::

    subscriber1.receive(_) >> "ok"

Here we use ``_`` to return ``"ok"`` no matter what message was passed as an argument. As usual, you can use any of the
other method argument constraints to control which invocations the interaction is going to match::

    subscriber1.receive("message1") >> "ok"
    subscriber1.receive("message2") >> "fail"

This will return ``"ok"`` whenever ``"message1"`` is received, and ``"fail"`` whenever
``"message2"`` is received. There is no limit as to which types can be returned provided they are
compatible with the method's declared return type.

Returning Sequences of Values
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To return different values on successive invocations, use the triple-right-shift (``>>>``) operator::

    subscriber1.receive(_) >>> ["ok", "error", "error", "ok"]

This will return ``"ok"`` for the first invocation, ``"error"`` for the second and third invocation,
and ``"ok"`` for all remaining invocations. The right-hand side must be a value that Groovy knows how to iterate over;
in this example, we've used a plain list.


Computing Return Values
~~~~~~~~~~~~~~~~~~~~~~~

To compute a return value based on the method's argument, use the the right-shift (``>>``) operator together with a closure.
If the closure declares a single untyped parameter, it gets passed the method's argument list::

    subscriber1.receive(_) >> { args -> args[0].size() > 3 ? "ok" : "fail" }

Here we return ``"ok"`` if the message is more than three characters long, and ``"fail"`` otherwise.

Often it would be nicer to have direct access to the method's arguments. If the closure declares more than one parameter
or a single *typed* parameter, method parameters will be mapped one-by-one to closure parameters::

    subscriber1.receive(_) >> { String message -> message.size() > 3 ? "ok" : "fail" }

This code is functionally equivalent to the previous one but more readable.

Performing Side Effects
~~~~~~~~~~~~~~~~~~~~~~~

Sometimes you may want to do more than just computing a return value. A typical example would be
to throw an exception. Again, closures come to the rescue::

    subscriber1.receive(_) >> { throw new InternalError("ouch") }

Of course, the closure can contain more code, for example a ``println`` statement. The code
will get executed every time the interaction matches an invocation.

Chaining Method Responses
~~~~~~~~~~~~~~~~~~~~~~~~~

Method responses can be chained::

    subscriber1.receive(_) >>> ["ok", "fail", "ok"] >>> { throw new InternalError() } >> "ok"

This will return ``"ok", "fail", "ok"`` for the first three invocations, throw ``InternalError``
for the fourth invocations, and return ``ok`` for any further invocations.

Combining Mocking and Stubbing
------------------------------

Mocking and stubbing go hand-in-hand::

    1 * subscriber1.receive("message1") >> "ok"
    1 * subscriber1.receive("message2") >> "fail"

When mocking and stubbing the same method call, it is important to express the two in a single interaction.
The following Mockito-style splitting of stubbing and mocking into two separate statements will *not* work::

    subscriber1.receive("message1")
    1 * subscriber1.receive("message1") >> "ok"

Because interactions are matched to invocations in declaration order, any invocation of ``receive``
with argument ``"message1"`` will match the first of the two interactions. Since that interaction
doesn't specify a response, the default value for the method's return type (``null`` in this case)
will be returned. (This is just another facet of Spock's lenient approach to mocking.) The second
interaction will never get a chance to match an invocation.

.. note:: Mocking and stubbing of the same method call has to happen in the same interaction.

Other Kinds of Mock Objects
---------------------------

So far, we have created mock objects with the ``MockingApi.Mock`` method. Aside from
this method, the ``MockingApi`` class provides a couple of other factory methods for creating
more specialized kinds of mock objects.

Stubs
~~~~~

A *stub* is created with the ``MockingApi.Stub`` factory method::

    def subscriber = Stub(Subscriber)

Whereas a mock can be used both for stubbing and mocking, a stub can only be used for stubbing.
Limiting a collaborator to a stub communicates its role to the readers of the specification.

.. note:: In case a stub invocation matches a mandatory interaction (that is, an interaction with a cardinality like ``1 *``),
          an ``InvalidSpecException`` is thrown.

Like a mock, a stub allows unexpected invocations. However, the values returned by a stub in such cases are more ambitious:

 * For primitive types, the primitive type's default value is returned.
 * For non-primitive numerical values (like ``BigDecimal``), zero is returned.
 * For non-numerical values, an "empty" or "dummy" object is returned. This could mean an empty String, an empty collection,
   an object constructed from its default constructor, or another stub which returns the same defaults.
   See class ``org.spockframework.mock.EmptyOrDummyResponder`` for the details.

Spies
~~~~~

A *spy* is created with the ``MockingApi.Spy`` factory method::

    def subscriber = Spy(SubscriberImpl, constructorArgs: ["Fred"])

A spy is always based on a real object. Hence you must provide a class type rather
than an interface type, along with any constructor arguments for the type.
If no constructor arguments are provided, the type's default constructor will be used.

Method calls on a spy are automatically delegated to the real object. Likewise, values
returned from the real object's methods are passed back to the caller via the spy.

After creating a spy, you can listen in on the conversation between the caller and the real object underlying the spy::

    1 * subscriber.receive(_)

Apart from making sure that ``receive`` gets called exactly once,
the conversation between the publisher and the ``SubscriberImpl`` instance underlying the spy remains unaltered.

When stubbing a method on a spy, the real method no longer gets called::

    subscriber.receive(_) >> "ok"

Instead of calling ``SubscriberImpl.receive``, the ``receive`` method will now simply return ``"ok"``.

Sometimes, it is desirable to both execute some code *and* delegate to the real method::

    subscriber.receive(_) >> { String message -> callRealMethod(); message.size() > 3 ? "ok" : "fail" }

Here we are using ``callRealMethod()`` to delegate the method invocation to the real object.
Note that we don't have to pass the ``message`` argument along; this is taken care of automatically. `callRealMethod()``
returns the real invocation's result, but in this example we opted to return our own result instead.
If we had wanted to pass a different message to the real method, we could have used ``callRealMethodWithArguments("changed message")``.

Groovy Mocks
------------

So far, all the mocking features we have seen work the same no matter if the calling code is written in Java or Groovy.
By leveraging Groovy's dynamic capabilities, Groovy mocks offer some additional features specifically for testing Groovy code.
They are created with the ``MockingApi.GroovyMock()``, ``MockingApi.GroovyStub()``, and ``MockingApi.GroovySpy()`` factory methods.

.. admonition:: When should I favor Groovy mocks over regular mocks?

   Groovy mocks should be used when the code under specification is written in Groovy *and*
   some of the unique Groovy mock features are needed. When called from Java code, Groovy mocks will behave like regular mocks. Note
   that it isn't necessary to use a Groovy mock merely because the code under specification and/or mocked interface or class type is written in Groovy.
   Unless you have a concrete reason to use a Groovy mock, prefer a regular mock.

Mocking Dynamic Methods
~~~~~~~~~~~~~~~~~~~~~~~

All Groovy mocks implement the ``GroovyObject`` interface. They support the mocking and stubbing of
dynamic methods as if they were physically declared methods::

    def subscriber = GroovyMock(Subscriber)

    1 * subscriber.someDynamicMethod("hello")

Mocking All Instances Of A Type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Usually, Groovy mocks are injected into the code under specification just like regular mocks.
However, when a Groovy mock is created as *global*, it automagically replaces all real instances
of the mocked type for the duration of the feature method [#automagic]_::

    def publisher = new Publisher()
    publisher << new RealSubscriber() << new RealSubscriber()

    def anySubscriber = GroovyMock(RealSubscriber, global: true)

    when:
    publisher.publish("message")

    then:
    2 * anySubscriber.receive("message")

Here, we set up the publisher with two instances of a real subscriber implementation.
Then we create a global mock of the *same* type. This reroutes all method calls on the
real subscribers to the mock object. The mock object's instance isn't ever passed to the publisher;
it is only used to describe the interaction.

 .. note:: Global mocks can only be created for a class type. They effectively replace
           all instances of that type for the duration of the feature method.

Since global mocks have a somewhat, well, global effect, it's often convenient
to use them together with ``GroovySpy``. This leads to the real code getting
executed *unless* an interaction matches, allowing you to selectively listen
in on objects and change their behavior just where needed.

Mocking Constructors
~~~~~~~~~~~~~~~~~~~~

Global mocks support mocking of constructors::

    def anySubscriber = GroovySpy(RealSubscriber, global: true)

    1 * new RealSubscriber("Fred")

Since we are using a spy, the object returned from the constructor call remains unchanged.
To change which object gets constructed, we can stub the constructor::

    new RealSubscriber("Fred") >> new RealSubscriber("Barney")

Now, whenever some code tries to construct a subscriber named Fred, we'll construct
a subscriber named Barney instead.

Mocking Static Methods
~~~~~~~~~~~~~~~~~~~~~~

Global mocks support mocking and stubbing of static methods::

    def anySubscriber = GroovySpy(RealSubscriber, global: true)

    1 * RealSubscriber.someStaticMethod("hello") >> 42

The same works for dynamic static methods.

When a global mock is used solely for mocking constructors and static methods,
the mock's instance isn't really needed. In such a case it's OK to just write::

    GroovySpy(RealSubscriber, global: true)

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

.. [#creating] See [Other Kinds Of Mock Objects]_ for more on this.

.. [#automagic] You may know this behavior from Groovy's ``MockFor()`` and ``StubFor()`` facilities.