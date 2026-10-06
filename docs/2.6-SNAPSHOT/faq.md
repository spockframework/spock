# Frequently Asked Questions (FAQ)

## What are Spock’s optional dependencies used for?

Spock runs fine without optional dependencies, hence the term *optional*. So what are they used for?


- Objenesis (artifact `org.objenesis:objenesis`) is required for mocking classes without default constructors.
- Either ByteBuddy (artifact `net.bytebuddy:byte-buddy`) or CGLIB (artifact `cglib:cglib-nodep`) are required for mocking classes and not just interfaces.


See also [Mocking Classes](interaction_based_testing.md#_mocking_classes).


## Why are there two options (ByteBuddy vs CGLIB) for mocking classes?

The main reason is backwards compatibility.
CGLIB came first historically, ByteBuddy was added later.
Both libraries basically provide identical features from a Spock user’s perspective.


If you are starting with Spock now, we recommend ByteBuddy as our preferred choice.


CGLIB is in legacy support mode, we will support it as long as the maintenance effort is relatively low.
So you may continue to use CGLIB for now if e.g. your tests use it in another context already.
If you have no compelling reasons to avoid ByteBuddy, you can just switch from CGLIB to ByteBuddy in an existing project.
Your tests should continue to run like before.


## Can Spock tests be compiled and run with GraalVM native-image?

Spock tests require dynamic Groovy language (and JVM) features and are not compatible with GraalVM `native-image`.
For details see [Issue #2169](https://github.com/spockframework/spock/issues/2169).

