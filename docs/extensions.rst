.. _Extensions:

Extensions
==========

Spock comes with a powerful extension mechanism, which allows to hook into a spec's lifecycle to enrich or alter its
behavior. In this chapter, we will first learn about Spock's built-in extensions, and then dive into writing custom
extensions.

Built-In Extensions
-------------------

Most of Spock's built-in extensions are *annotation-driven*. In other words, they are triggered by annotating a
spec class or method with a certain annotation. You can tell such an annotation by its ``@ExtensionAnnotation``
meta-annotation.

Ignore
~~~~~~

To temporarily prevent a feature method from getting executed, annotate it with ``spock.lang.Ignore``::

    @Ignore
    def "my feature"() { ... }

For documentation purposes, a reason can be provided::

    @Ignore(reason = "TODO")
    def "my feature"() { ... }

To ignore a whole specification, annotate its class::

    @Ignore
    class MySpec extends Specification { ... }

In most execution environments, ignored feature methods and specs will be reported as "skipped".

IgnoreRest
~~~~~~~~~~

To ignore all but a (typically) small subset of methods, annotate the latter with ``spock.lang.IgnoreRest``::

    def "I'll be ignored"() { ... }

    @IgnoreRest
    def "I'll run"() { ... }

    def "I'll also be ignored"() { ... }

``@IgnoreRest`` is especially handy in execution environments that don't provide an (easy) way to run a subset of methods.

IgnoreIf
~~~~~~~~

To ignore a feature method under certain conditions, annotate it with ``spock.lang.IgnoreIf``,
followed by a predicate::

    @IgnoreIf({ System.getProperty("os.name").contains("windows") })
    def "I'll run everywhere but on Windows"() { ... }

To make predicates easier to read and write, the following properties are available inside the closure:

 * ``sys`` A map of all system properties
 * ``env`` A map of all environment variables
 * ``os`` Information about the operating system (see ``spock.util.environment.OperatingSystem``)
 * ``jvm`` Information about the JVM (see ``spock.util.environment.Jvm``)

Using the ``os`` property, the previous example can be rewritten as::

    @IgnoreIf({ os.windows })
    def "I'll run everywhere but on Windows"() { ... }

Requires
~~~~~~~~

To execute a feature method under certain conditions, annotate it with ``spock.lang.Requires``,
followed by a predicate::

    @Requires({ os.windows })
    def "I'll only run on Windows"() { ... }

``Requires`` works exactly like ``IgnoreIf``, except that the predicate is inverted. In general, it is preferable
to state the conditions under which a method gets executed, rather than the conditions under which it gets ignored.

FailsWith
~~~~~~~~~~~

If you have a feature that you would like to verify whether it fails with a particular exception, you can annotate the said feature with ``spock.lang.FailsWith``,
followed by the type of Exception that the said feature is expected to throw::

    @FailsWith(TimeoutException)
    def "I fail with TimeoutException"() { ... }

The annotation can also be applied at Spec level, once applied it will be applicable for all the feature methods that are not already annotated with FailsWith.

See
~~~~~~~

If you want to provide a link to external information for a Spec or a feature inside the spock report, you can do so by annotating the feature or spec with ``spoc.lang.See``, followed by an array of url::

    @See( ["http://en.wikipedia.org/wiki/Uniform_resource_locator","http://en.wikipedia.org/wiki/Wikipedia:External_links"] )
    def "I have reference to external links"() { ... }

The external links provided to the annotation are shown under the section "Attachments" for a feature or spec in the spock report. These links hen clicked in the report opens the said reference link in an iframe window inside the report page.


Timeout
~~~~~~~~~

If you want to timeout an execution of a feature, fixture or a Spec after specified amount of time you can do so by annotating the said feature, fixture or spec with ``spock.lang.Timeout``, followed by time period after which the execution should timeout::

    @Timeout(60)
    def "I will fail with timeout if I dont execute within the specified time"() { ... }

The timeout mentioned should be provided in integer format. By default the time period for timeout is calculated in 'seconds' but if you want to use some other time unit you can do so by using the TimeUnit enum of ``java.util.concurrent.TimeUnit`` available with JDK::

    @Timeout(value = 600, unit = TimeUnit.MILLISECONDS)
    def "I fail with timeout error if I don't get executed within 600 miliseconds"(){ ... }
    

TODO More to follow.

Writing Custom Extensions
-------------------------

TODO

