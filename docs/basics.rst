.. _Basics:

Basic Specifications
====================

A specification is represented as a Groovy class that extends from ``spock.lang.Specification``.
The name of a specification usually relates to the system or system operation described
by the specification. For example, ``CustomerSpec``, ``H264VideoPlayback`` and
``ASpaceshipAttackedFromTwoSides`` are all reasonable names for a specification.

Tests belonging to a specification are performed in `feature methods`_. The minimal
feature method has its description defined by its name and is structured into one or more
`blocks`_. This is an example of a basic Spock specification::

    import spock.lang.Specification

    class MathSpec extends Specification {
        def "maximum of two numbers"() {
            expect:
            Math.max(1, 3) == 3
        }
    }

In addition to feature methods, a specification may also include `fixture methods`_ to
help with its setup and cleanup, `helper methods`_ to reduce code duplication and 
`fields`_ that can hold data used in multiple feature methods.

Fields
------
To use an object in multiple feature methods, you can declare *instance fields* on the
specification class. It is good practice to initialize them right at the point of 
declaration [#initializeFields]_::

    class SomeSpec extends Specification {
        def obj = new ClassUnderSpecification()
        def coll = new Collaborator()
        
        //rest of the class follows here
    }

Objects stored into instance fields are **not shared between feature methods**.
Instead, every feature method gets its own object. This helps to isolate feature methods
from each other, which is often a desirable goal, but sometimes you need to share an object
between feature methods. For example, the object might be very expensive to create, or you
might want your feature methods to interact with each other. To achieve this, declare a 
``@Shared`` field. Again it's best to initialize the field right at the point of declaration
[#initializeSharedFields]_::

    @Shared res = new VeryExpensiveResource()

You could also use static fields for this purpose, but this is discouraged because ``@Shared``
field semantics with respect to sharing are more well-defined. Therefore static fields should
only be used for constants::

    static final PI = 3.141592654

Fixture methods
---------------
Fixture methods are responsible for setting up and cleaning up the environment in which feature
methods are run. Usually it's a good idea to use a fresh fixture for every feature method, which
is what the ``setup()`` and ``cleanup()`` methods are for. Occasionally it makes sense for feature
methods to share a fixture, which is achieved by using shared fields together with the 
``setupSpec()`` and ``cleanupSpec()`` methods. All fixture methods are optional.

Note: The ``setupSpec()`` and ``cleanupSpec()`` methods may not reference instance fields.

Feature Methods
---------------
Feature methods are the heart of a specification. They describe the features (properties,
aspects) that you expect to find in the system under specification. By convention, 
feature methods are named with String literals. Try to choose good names for your feature
methods, and feel free to use any characters you like!

Conceptually, a feature method consists of four block types:

* Set up the feature's fixture
* Provide a stimulus to the system under specification
* Describe the response expected from the system
* Clean up the feature's fixture

Whereas the first and last types are optional, the stimulus and response phases are always
present (except in interacting feature methods), and may occur more than once.

Blocks
------

Helper methods
--------------




.. rubric:: Footnotes

.. [#initializeFields] Semantically, initializing instance fields on declaration
   is equivalent to initializing them at the very beginning of the ``setup()`` method.
   
.. [#initializeSharedFields] Semantically, initializing shared fields on declaration
   is equivalent to initializing the field at the very beginning of the ``setupSpec()`` method.