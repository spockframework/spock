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

How are we going to test ``Publisher``? With state-based testing we can easily verify that the publisher correctly
manages its list of subscribers. The more interesting question, though, is whether a message sent by the publisher
is received by all registered subscribers. To answer this question, we need a special implementation of
``Subscriber`` that listens in on the conversation between the publisher and its subscribers. Such an
implementation is often called a ``mock object``.

While we could certainly create a mock implementation of ``Subscriber`` by hand, writing and maintaining this code
can get unpleasant as the number of methods and complexity of interactions increases. This is where mocking frameworks
come in. They allow to succinctly describe the expected interactions between the object under test and its
collaborators. Mock implementations are synthesized on the fly based on this description.

The Java world boasts a number of popular and mature mocking frameworks: JMock, EasyMock, Mockito, to name just a few.
Although any of these frameworks can be used together with Spock, we didn’t stop here. Instead, Spock comes with
its own mocking framework that leverages the power of Groovy to make interaction-based tests easier to write,
more readable, and ultimately more fun.

    .. note:: All features of Spock's mocking framework can be used to, and work the same for, testing both Java
    and Groovy code.

