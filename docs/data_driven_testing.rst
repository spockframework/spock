Data Driven Testing
===================

Oftentimes, it can be useful to exercise the same code multiple times, with varying inputs and expected results. Spock's data driven testing support turns this into a first class feature.

Introduction
------------

Suppose we want to specify the behavior of the Math.max method. This could look as follows::

    class MathSpec extends Specification {
        def "maximum of two numbers"() {
            expect:
            // exercise the math method for a few different inputs
            Math.max(1, 3) == 3
            Math.max(7, 4) == 7
            Math.max(0, 0) == 0
        }
    }

Although this approach may be fine in simple cases like this one, it has some potential drawbacks:

* Code and data are mixed and cannot easily be changed independently
* Data cannot easily be generated or fetched from an external source
* In order to exercise the same code multiple times, it either has to be duplicated or extracted into a separate method
* In case of a failure, it isn't immediately clear which inputs caused the failure
* Exercising the same code multiple times does not benefit from the same isolation as executing different methods does

Spock's data-driven testing support tries to address these concerns. To get started, let's refactor above code into a data-driven specification method. First, we introduce three method parameters (called _data variables_) that replace the hard-coded integer values::

    class MathSpec extends Specification {
        def "maximum of two numbers"(int a, int b, int c) {
            expect:
            Math.max(a, b) == c
        }
    }

Now, where do the values for these data variables come from? The answer is that a data-driven specification method has an additional block, the ``where`` block, which binds values to data variables.

Data tables
-----------

One way of binding values to data variables is a _data table_. Data tables are the perfect choice for exercising code with a fixed set of values::

    class Math extends Specification {
        def "maximum of two numbers"(int a, int b, int c) {
            expect:
            Math.max(a, b) == c

            where:
            a | b | c
            1 | 3 | 3
            7 | 4 | 4
            0 | 0 | 0
        }
    }

The first line of the data table is the table header, and lists all data variables. The subsequent lines are the table rows. Each row defines a binding of values to data variables. The specification method will be executed once for each row - three times in this example. Each method execution is called an iteration. If an iteration fails, the remaining iterations will nevertheless be executed, and all failures will be reported.

Isolated execution
------------------

Iterations are isolated from each other as if they were separate specification methods. Each iteration will get its own instance of the specification class, and the setup and cleanup methods will be called before and after each iteration, respectively. In order to share a field between iterations, the field has to be @Shared or static, and has to be initialized with a field initializer or the setupSpec method*.

Syntactic variations
--------------------

The previous code can be tweaked in a few ways. First, since a where block already lists all data variables, declaring them as method parameters is optional (but is a good mental model to have).* Second, inputs and expected outputs can be separated with a double pipe (||) to visually set them apart. With this, the code becomes::

     class DataDriven extends Specification {
          def "maximum of two numbers"() {
              expect:
              Math.max(a, b) == c

              where:
              a | b || c
              3 | 5 || 5
              7 | 0 || 7
              0 | 0 || 0
          }
      }

Failure output
--------------

Let's assume that our implementation of the max method has a flaw, and one of the iterations fails::

    maximum of two numbers   FAILED

    Condition not satisfied:

    Math.max(a, b) == c
        |    |  |  |  |
        |    7  0  |  7
        42         false

Now the obvious question is: Which iteration failed, and which row in the data table does it correspond to? In our example, it isn't that hard to figure out that it's the second iteration that failed. At other times this can be more difficult or even impossible.* In any case, it would be nice if Spock made it loud and clear which iteration failed, rather than just reporting the failure. This is the purpose of the @Unroll annotation.

Unrolling iterations
--------------------

    @Unroll
    def "maximum of two numbers"() { ... }

Unrolling a data-driven specification method results in each iteration being reported independently. Note that unrolling has no effect on the execution itself - it is only an alternation in reporting. Depending on your execution environment, the output will change to something like::

    maximum of two numbers[0]   PASSED
    maximum of two numbers[1]   FAILED

    Math.max(a, b) == c
        |    |  |  |  |
        |    7  0  |  7
        42         false

    maximum of two numbers[2]   PASSED

We can now immediately see that the second iteration (with index 1) failed. With a bit of effort, we can do even better::

    @Unroll
    def "maximum of #a and #b is #c"() { ... }

The method name is now a naming pattern that refers to data variables a, b, and c. The variables are marked with a hash sign and will be substituted with their values in the report::

    maximum of 3 and 5 is 5   PASSED
    maximum of 7 and 0 is 7   FAILED

    Math.max(a, b) == c
        |    |  |  |  |
        |    7  0  |  7
        42         false

    maximum of 0 and 0 is 0   PASSED

Now we can tell at a glance that the problem is caused by inputs (7, 0).

Other forms of data bindings
----------------------------

Data tables aren't the only way to bind values to data variables. In fact, they are a special case of a standard binding::

    ...
    where:
    a << [3, 7, 0]
    b << [5, 0, 0]
    c << [5, 7, 0]

The left-shift (<<) operator is used to hook up a data variable with a _data provider_. Any object that Groovy knows how to iterate over can be used as a data provider. This includes ``Collection``s, ``String``s, ``Iterable``s, and objects implementing the ``Iterable`` contract Because data providers can be constructed from arbitrary expressions, it is also possible to fetch data from external sources like databases and spreadsheets, or to generate data automatically. Data providers will be iterated over lazily, fetching the next value before each iteration.

Multi-bindings
--------------

If a data provider returns multiple values at once, they can be assigned to multiple variables simultaneously::

    @Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

    def "maximum of two numbers"() {
        ...
        where:
        [a, b, c] << sql.rows("select a, b, c from maxdata")
    }

The sql variable has been declared as @Shared, which means that the same instance is shared among all iterations.

    ..info:: Only @Shared and static variables can be accessed from a where block.

If the data source returns more values than necessary, some of them can be ignored:

    ...
    where:
    [a, b, _, c] << sql.rows("select * from maxdata")

Derived parameterizations
-------------------------

To produce new data values out of others, the assignment (=) operator can be used::

    ...
    where:
    row << sql.rows("select * from maxdata")
    // pick out a, b, and c
    a = row.a
    b = row.b
    c = row.c

Number of iterations
--------------------

A data-driven specification method will produce new iterations until there is no more data. As a consequence, successive executions of the same method can potentially produce different numbers of iterations. If a data provider runs out of values sooner than its peers, an exception will occur. Derived parameterizations don't affect the number of iterations. A method with only derived parameterizations will produce exactly one iteration.

Closing of data providers
-------------------------

After all iterations have completed, the zero-argument close method will be called on all data providers that have such a method.

More on unroll naming patterns
------------------------------

Method naming patterns are similar to Groovy GStrings, except for the following differences:

# Expressions are denoted with # instead of $, and there is no equivalent for the ${...} syntax
# Expressions only support property access and zero-args method calls

Given a class Person with properties name and age, and a data variable person of type Person, the following are valid naming patterns:

# def "#person is #person.age years old"() { ... } // property access
# def "#person.name.toUpperCase()"() { ... } // zero-arg method call

Non-string values (like #person above) are converted to Strings according to Groovy semantics. The following are examples for invalid naming patterns:

# def "#person.name.split(' ')[1]" { ... } // cannot have method arguments
# def "#person.age / 2" { ... } // cannot use operators

Although such patterns should be rarely needed, they can be made to work by introducing extra data variables, which can hold arbitrary expressions::

    def "#lastName"() {
        ...
        where:
        person = ...
        lastName = person.name.split(' ')[1]
    }



    ..sidebar:: Why isn't @Unroll the default?





*One advantage of declaring data variables and their types as method parameters is that it can improve IDE support. However, recent versions of IDEA are smart enough to recognize data variables automatically, and even infer their types from the values in the data table.

*For example, a specification method could use data variables only in its setup block.

*Groovy syntax does not allow a dollar sign in a method name.


