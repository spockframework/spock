.. _Extensions:

Extensions
==========

Spock comes with a powerful SPI that allows to hook into a spec's lifecycle and enrich or alter its behavior.
In this chapter, we will first learn about Spock's built-in extensions, and then dive into writing custom extensions.

Built-In Extensions
-------------------

Most of Spock's built-in extensions are activated by annotating a spec or feature method. Unless
noted otherwise, the corresponding annotation types are declared in the ``spock.lang`` package.

Ignore
~~~~~~

To temporarily prevent a feature method from getting executed, annotate it with ``@Ignore``::

    @Ignore
    def "my feature"() { ... }

For documentation purposes, a reason can be provided::

    @Ignore(reason = "TODO")
    def "some feature"() { ... }

To ignore a whole specification, annotate its class::

    @Ignore
    class MySpec extends Specification { ... }

In most execution environments, ignored feature methods and specs will be reported as "skipped".

IgnoreRest
~~~~~~~~~~

To ignore all but a (typically small) subset of methods, annotate the latter with ``@IgnoreRest``::

    def "I'll be ignored"() { ... }

    @IgnoreRest
    def "I'll run"() { ... }

    def "I'll also be ignored"() { ... }

``@IgnoreRest`` is especially handy in execution environments that don't provide an (easy) way to run a subset of methods.

IgnoreIf
~~~~~~~~

To prevent a feature method from getting executed under certain circumstances, annotate it with ``@IgnoreIf``, followed
by a predicate::

    @IgnoreIf({ System.getProperty("os.name").contains("windows") })
    def "I'll run everywhere but on Windows"() { ... }

To make predicates easier to read and write, the following properties are available inside the predicate's code block::

 * ``sys`` A map of all system properties
 * ``env`` A map of all environment variables
 * ``os`` Information about the operating system (see ``spock.util.environment.OperatingSystem``)
 * ``jvm`` Information about the JVM (see ``spock.util.environment.Jvm``)

Using these properties, the previous example can be rewritten as::

    @IgnoreIf({ os.windows })
    def "my feature"() { ... }

Requires
~~~~~~~~

``@Requires`` is the same as ``@IgnoreIf``, except that the predicate is inverted::

        @Requires({ os.windows })
        def "I'll only run on Windows"() { ... }

Because it positively states the necessary preconditions for a feature method to get executed, ``@Requires`` should,
in general, be preferred over ``@IgnoreIf``.

TODO More to follow.

Writing Custom Extensions
-------------------------

TODO

