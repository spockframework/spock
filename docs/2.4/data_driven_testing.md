# Data Driven Testing

Oftentimes, it is useful to exercise the same test code multiple times, with varying inputs and expected results.
Spock’s data driven testing support makes this a first class feature.


## Introduction

Suppose we want to specify the behavior of the `Math.max` method:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"() {
    expect:
    // exercise math method for a few different inputs
    Math.max(1, 3) == 3
    Math.max(7, 4) == 7
    Math.max(0, 0) == 0
  }
}
```


Although this approach is fine in simple cases like this one, it has some potential drawbacks:


- Code and data are mixed and cannot easily be changed independently
- Data cannot easily be auto-generated or fetched from external sources
- In order to exercise the same code multiple times, it either has to be duplicated or extracted into a separate method
- In case of a failure, it may not be immediately clear which inputs caused the failure
- Exercising the same code multiple times does not benefit from the same isolation as executing separate methods does


Spock’s data-driven testing support tries to address these concerns. To get started, let’s refactor above code into a
data-driven feature method. First, we introduce three method parameters (called *data variables*) that replace the
hard-coded integer values:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"(int a, int b, int c) {
    expect:
    Math.max(a, b) == c
    ...
  }
}
```


We have finished the test logic, but still need to supply the data values to be used. This is done in a `where:` block,
which always comes at the end of the method. In the simplest (and most common) case, the `where:` block holds a *data table*.


## Data Tables

Data tables are a convenient way to exercise a feature method with a fixed set of data values:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"(int a, int b, int c) {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 3 | 3
    7 | 4 | 7
    0 | 0 | 0
  }
}
```


The first line of the table, called the *table header*, declares the data variables. The subsequent lines, called
*table rows*, hold the corresponding values. For each row, the feature method will get executed once; we call this an
*iteration* of the method. If an iteration fails, the remaining iterations will nevertheless be executed. All
failures will be reported.


Data tables must have at least two columns. A single-column table can be written as:


```groovy
where:
a | _
1 | _
7 | _
0 | _
```


A sequence of two or more underscores can be used to split one wide data table into multiple narrower ones.
Without this separator and without any other data variable assignment in between there
is no way to have multiple data tables in one `where` block, the second table would just
be further iterations of the first table, including the seemingly header row:


```groovy
where:
a | _
1 | _
7 | _
0 | _
__

b | c
1 | 2
3 | 4
5 | 6
```


This is semantically exactly the same, just as one wider joined data table:


```groovy
where:
a | b | c
1 | 1 | 2
7 | 3 | 4
0 | 5 | 6
```


The sequence of two or more underscores can be used anywhere in the `where` block.
It will be ignored everywhere, except for in between two data tables, where it is
used to separate the two data tables. This means that the separator can also be used
as styling element in different ways. It can be used as separator line like shown in
the last example or it can for example be used visually as top border of tables
additionally to its effect of separating them:


```groovy
where:
_____
a | _
1 | _
7 | _
0 | _
_____
b | c
1 | 2
3 | 4
5 | 6
```


## Isolated Execution of Iterations

Iterations are isolated from each other in the same way as separate feature methods. Each iteration gets its own instance
of the specification class, and the `setup` and `cleanup` methods will be called before and after each iteration,
respectively.


## Sharing of Objects between Iterations

In order to share an object between iterations, it has to be kept in a `@Shared` or static field.


> [!NOTE]
> Only `@Shared` and static variables can be accessed from within a `where:` block.


Note that such objects will also be shared with other methods. There is currently no good way to share an object
just between iterations of the same method. If you consider this a problem, consider putting each method into a separate
spec, all of which can be kept in the same file. This achieves better isolation at the cost of some boilerplate code.


## Syntactic Variations

The previous code can be tweaked in a few ways.


First, since the `where:` block already declares all data variables, the method parameters can be
omitted. (Note: The idea behind allowing method parameters is to enable better IDE support. However, recent versions of IntelliJ IDEA recognize data variables automatically, and even infer their types from the values contained in the data table.)


You can also omit some parameters and specify others, for example to have them typed.
The order also is not important, data variables are matched by name to the specified method parameters.


Second, inputs and expected outputs can be separated with a double pipe symbol (`||`) to visually set them apart.


With this, the code becomes:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0
  }
}
```


Alternatively to using single or double pipes you can also use any amount of semicolons to separate data columns
from each other:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"() {
    expect:
    Math.max(a, b) == c

    where:
    a ; b ;; c
    1 ; 3 ;; 3
    7 ; 4 ;; 7
    0 ; 0 ;; 0
  }
}
```


Pipes and semicolons as data column separator can not be mixed within one table.
If the column separator changes, this starts a new stand-alone data table:


```groovy
class MathSpec extends Specification {
  def "maximum of two numbers"() {
    expect:
    Math.max(a, b) == c
    Math.max(d, e) == f

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0

    d ; e ;; f
    1 ; 3 ;; 3
    7 ; 4 ;; 7
    0 ; 0 ;; 0
  }
}
```


## Reporting of Failures

Let’s assume that our implementation of the `max` method has a flaw, and one of the iterations fails:


```
maximum of two numbers [a: 1, b: 3, c: 3, #0]   PASSED
maximum of two numbers [a: 7, b: 4, c: 7, #1]   FAILED

Condition not satisfied:

Math.max(a, b) == c
|    |   |  |  |  |
|    |   7  4  |  7
|    42        false
class java.lang.Math

maximum of two numbers [a: 0, b: 0, c: 0, #2]   PASSED
```


The obvious question is: Which iteration failed, and what are its data values? In our example, it isn’t hard to figure
out that it’s the second iteration (with index 1) that failed even from the rich condition rendering. At other times
this can be more difficult or even impossible. (Note: For example, a feature method could use data variables in its `given:` block, but not in any conditions.) In any case, Spock makes it loud and clear which iteration failed, rather
than just reporting the failure. Iterations of a feature method are by default unrolled with a rich naming pattern.
This pattern can also be configured as documented at [_unrolled_iteration_names](#_unrolled_iteration_names) or the unrolling can be disabled
like described in the following section.


## Method Uprolling and Unrolling

A method annotated with `@Rollup` will have its iterations not reported independently but only aggregated within the
feature. This can for example be used if you produce many test cases from calculations or if you use external data
like the contents of a database as test data and do not want the test count to vary:


```groovy
@Rollup
def "maximum of two numbers"() {
...
```


Note that up- and unrolling has no effect on how the method gets executed; it is only an alternation in reporting.
Depending on the execution environment, the output will look something like:


```
maximum of two numbers   FAILED

Condition not satisfied:

Math.max(a, b) == c
|    |   |  |  |  |
|    |   7  4  |  7
|    42        false
class java.lang.Math
```


The `@Rollup` annotation can also be placed on a spec.
This has the same effect as placing it on each data-driven feature method of the spec that does not have an
`@Unroll` annotation.


Alternatively the [configuration file](extensions.md#spock-configuration-file) setting `unrollByDefault`
in the `unroll` section can be set to `false` to roll up all features automatically unless
they are annotated with `@Unroll` or are contained in an `@Unroll`ed spec and thus reinstate the pre Spock 2.0
behavior where this was the default.


**Disable Default Unrolling**

```groovy
unroll {
    unrollByDefault false
}
```


It is illegal to annotate a spec or a feature with both the `@Unroll` and the `@Rollup` annotation and if detected
this will cause an exception to be thrown.


---


To summarize:


A feature will be uprolled


- if the method is annotated with `@Rollup`
- if the method is not annotated with `@Unroll` and the spec is annotated with `@Rollup`
- if neither the method nor the spec is annotated with `@Unroll`
and the configuration option `unroll { unrollByDefault }` is set to `false`


A feature will be unrolled


- if the method is annotated with `@Unroll`
- if the method is not annotated with `@Rollup` and the spec is annotated with `@Unroll`
- if neither the method nor the spec is annotated with `@Rollup`
and the configuration option `unroll { unrollByDefault }` is set to its default value `true`


## Data Pipes

Data tables aren’t the only way to supply values to data variables. In fact, a data table is just syntactic sugar for
one or more *data pipes*:


```groovy
...
where:
a << [1, 7, 0]
b << [3, 4, 0]
c << [3, 7, 0]
```


A data pipe, indicated by the left-shift (`<<`) operator, connects a data variable to a *data provider*. The data
provider holds all values for the variable, one per iteration. Any object that Groovy knows how to iterate over can be
used as a data provider. This includes objects of type `Collection`, `String`, `Iterable`, and objects implementing the
`Iterable` contract. Data providers don’t necessarily have to *be* the data (as in the case of a `Collection`);
they can fetch data from external sources like text files, databases and spreadsheets, or generate data randomly.
Data providers are queried for their next value only when needed (before the next iteration).


> [!NOTE]
> Spock uses the `size()` method to calculate the amount of iterations,
>       except for data providers that implement `Iterator`,
>       so make sure `size()` is working efficient, or supply an `Iterator` if that is not possible.


## Multi-Variable Data Pipes

If a data provider returns multiple values per iteration (as an object that Groovy knows how to iterate over),
it can be connected to multiple data variables simultaneously. The syntax is somewhat similar to Groovy multi-assignment
but uses brackets instead of parentheses on the left-hand side:


```groovy
@Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

def "maximum of two numbers"() {
  expect:
  Math.max(a, b) == c

  where:
  [a, b, c] << sql.rows("select a, b, c from maxdata")
}
```


Data values that aren’t of interest can be ignored with an underscore (`_`):


```groovy
...
where:
[a, b, _, c] << sql.rows("select * from maxdata")
```


The multi-assignments can even be nested. The following example will generate these iterations:


| a | b | c |
| --- | --- | --- |
| `['a1', 'a2']` | ’b1'` | ’c1'` |
| `['a2', 'a1']` | ’b1'` | ’c1'` |
| `['a1', 'a2']` | ’b2'` | ’c2'` |
| `['a2', 'a1']` | ’b2'` | ’c2'` |


```groovy
...
where:
[a, [b, _, c]] << [
  ['a1', 'a2'].permutations(),
  [
    ['b1', 'd1', 'c1'],
    ['b2', 'd2', 'c2']
  ]
].combinations()
```


### Named deconstruction of data pipes

Since Spock 2.2, multi variable data pipes can also be deconstructed from maps.
This is useful when the data provider returns a map with named keys.
Or, if you have long values that don’t fit well into a data-table, then using the maps makes it easier to read.


```groovy
...
where:
[a, b, c] << [
  [
    a: 1,
    b: 3,
    c: 5
  ],
  [
    a: 2,
    b: 4,
    c: 6
  ]
]
```


You can use named deconstruction with nested data pipes, but only on the innermost nesting level.


```groovy
...
where:
[a, [b, c]] << [
  [1, [b: 3, c: 5]],
  [2, [c: 6, b: 4]]
]
```


## Cross-multiplying Data Providers

Two or more consecutive data providers, be it a data table or a data pipe, can also be combined
using a cartesian product using the `combined:` label between them. The following will result
in these executed tests:


- `feature [a: 1, b: 3, c: 5, d: 1, e: 4, #0]`
- `feature [a: 1, b: 4, c: 6, d: 1, e: 5, #1]`
- `feature [a: 2, b: 3, c: 5, d: 1, e: 5, #2]`
- `feature [a: 2, b: 4, c: 6, d: 1, e: 6, #3]`


```groovy
where:
a | _
1 | _
2 | _

combined:

b | c
3 | 5
4 | 6

combined:

d | _
1 | _

e = a + b
```


Exactly the same result can be achieved using


```groovy
where:
a << [1, 2]
combined:
b | c
3 | 5
4 | 6

d = 1
e = a + b
```


> [!NOTE]
> Combining with a derived data variable (`x = …`) makes no sense and thus is forbidden.


> [!NOTE]
> If a data table takes part in a cross-multiplication, accessing columns of previous
>       data tables would not behave in an intuitive way and thus is currently forbidden. Accessing
>       previous columns within one data table even works while taking part in a cross-multiplication.


> [!NOTE]
> Only the data provider right before the `combined:` label is
> combined with the data provider right after the `combined:` label.
> 
> 
> So if you execute
> 
> 
> ```groovy
> where:
> a << [1, 2, 3, 4, 5, 6]
> b << [5, 6]
> combined:
> c << [7, 8, 9]
> ```
> 
> 
> it will result in these executed tests:
> 
> 
> - `feature [a: 1, b: 5, c: 7, #0]`
> - `feature [a: 2, b: 5, c: 8, #1]`
> - `feature [a: 3, b: 5, c: 9, #2]`
> - `feature [a: 4, b: 6, c: 7, #3]`
> - `feature [a: 5, b: 6, c: 8, #4]`
> - `feature [a: 6, b: 6, c: 9, #5]`
> 
> 
> If you want to combine `a` and `b` with `c` to get this result:
> 
> 
> - `feature [a: 1, b: 3, c: 5, #0]`
> - `feature [a: 1, b: 3, c: 6, #1]`
> - `feature [a: 2, b: 4, c: 5, #2]`
> - `feature [a: 2, b: 4, c: 6, #3]`
> 
> 
> you have to for example use
> 
> 
> ```groovy
> where:
> [a, b] << [
>   [1, 2],
>   [3, 4]
> ].transpose()
> combined:
> c << [5, 6]
> ```
> 
> 
> or
> 
> 
> ```groovy
> where:
> a | b
> 1 | 3
> 2 | 4
> combined:
> c << [5, 6]
> ```
> 
> 


## Data Variable Assignment

A data variable can be directly assigned a value:


```groovy
...
where:
a = 3
b = Math.random() * 100
c = a > b ? a : b
```


Assignments are re-evaluated for every iteration. As already shown above, the right-hand side of an assignment may refer
to other data variables:


```groovy
...
where:
row << sql.rows("select * from maxdata")
// pick apart columns
a = row.a
b = row.b
c = row.c
```


## Accessing Other Data Variables

There are only two possibilities to access one data variable from the calculation
of another data variable.


The first possibility are derived data variables like shown in the last section.
Every data variable that is defined by a direct assignment can access all
previously defined data variables, including the ones defined through data
tables or data pipes:


```groovy
...
where:
a = 3
b = Math.random() * 100
c = a > b ? a : b
```


The second possibility is to access previous columns within data tables:


```groovy
...
where:
a | b
3 | a + 1
7 | a + 2
0 | a + 3
```


This also includes columns in previous data tables in the same `where` block:


```groovy
...
where:
a | b
3 | a + 1
7 | a + 2
0 | a + 3

and:
c = 1

and:
d     | e
a * 2 | b * 2
a * 3 | b * 3
a * 4 | b * 4
```


## Multi-Variable Assignment

Like with data pipes, you can also assign to multiple variables in one expression, if you have some object Groovy
can iterate over. Unlike with data pipes, the syntax here is identical to standard Groovy multi-assignment syntax:


```groovy
@Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

def "maximum of two numbers multi-assignment"() {
  expect:
  Math.max(a, b) == c

  where:
  row << sql.rows("select a, b, c from maxdata")
  (a, b, c) = row
}
```


Data values that aren’t of interest can be ignored with an underscore (`_`):


```groovy
...
where:
row << sql.rows("select * from maxdata")
(a, b, _, c) = row
```


## Combining Data Tables, Data Pipes, and Variable Assignments

Data tables, data pipes, and variable assignments can be combined as needed:


```groovy
...
where:
a | b
1 | a + 1
7 | a + 2
0 | a + 3

c << [3, 4, 0]

d = a > c ? a : c
```


## Type Coercion for Data Variable Values

Data variable values are coerced to the declared parameter type using
[type coercion](https://groovy-lang.org/operators.html#_coercion_operator). Due to that custom type conversions can be
provided as [extension module](https://groovy-lang.org/metaprogramming.html#_extension_modules) or with the help of
the [`@Use`](extensions.md#_use) extension on the specification (as it has no effect to the `where:` block if
applied to a feature).


```groovy
def "type coercion for data variable values"(Integer i) {
  expect:
  i instanceof Integer
  i == 10

  where:
  i = "10"
}
```


```groovy
@Use(CoerceBazToBar)
class Foo extends Specification {
  def foo(Bar bar) {
    expect:
    bar == Bar.FOO

    where:
    bar = Baz.FOO
  }
}
enum Bar { FOO, BAR }
enum Baz { FOO, BAR }
class CoerceBazToBar {
  static Bar asType(Baz self, Class<Bar> clazz) {
    return Bar.valueOf(self.name())
  }
}
```


## Number of Iterations

The number of iterations depends on how much data is available. Successive executions of the same method can
yield different numbers of iterations. If a data provider runs out of values sooner than its peers, an exception will occur.
Variable assignments don’t affect the number of iterations. A `where:` block that only contains assignments yields
exactly one iteration.


## Filtering iterations

If you want to filter out some iterations, you can use the `@IgnoreIf` annotation on the feature method.
This has one significant drawback though, the iteration would be reported as skipped in test reports.
Therefor you can have a `filter` block after the `where` block.
The content of this block is treated like the content of the `expect` block.
If any of the implicit or explicit assertions in the `filter` block fails, the iteration is treated like it would not exist.
This also means, that if all iterations are filtered out, the test will fail like when giving a data provider without content.


In the following example the test is executed with the values `1`, `2`, `4`, and `5` for the variable `i`,
the iteration where `i` would be `3` is filtered out by the `filter` block:


```groovy
def "excluding iterations"() {
  expect:
  i in ((1..5) - 3)

  where:
  i << (1..5)

  filter:
  i != 3
}
```


## Closing of Data Providers

After all iterations have completed, the zero-argument `close` method is called on all data providers that have
such a method.


## Unrolled Iteration Names

By default, the names of unrolled iterations are the name of the feature, plus the data variables and the iteration
index. This will always produce unique names and should enable you to identify easily the failing data variable
combination.


The example at [_reporting_of_failures](#_reporting_of_failures) for example shows with `maximum of two numbers [a: 7, b: 4, c: 7, #1]`,
that the second iteration (`#1`) where the data variables have the values `7`, `4` and `7` failed.


With a bit of effort, we can do even better:


```groovy
def "maximum of #a and #b is #c"() {
...
```


This method name uses placeholders, denoted by a leading hash sign (`#`), to refer to data variables `a`, `b`, and `c`.
In the output, the placeholders will be replaced with concrete values:


```
maximum of 1 and 3 is 3   PASSED
maximum of 7 and 4 is 7   FAILED

Math.max(a, b) == c
|    |   |  |  |  |
|    |   7  4  |  7
|    42        false
class java.lang.Math

maximum of 0 and 0 is 0   PASSED
```


Now we can tell at a glance that the `max` method failed for inputs `7` and `4`.


An unrolled method name is similar to a Groovy `GString`, except for the following differences:


- Expressions are denoted with `#` instead of `$`, and there is no equivalent for the `${…}` syntax.
- Expressions only support property access and zero-arg method calls.


Given a class `Person` with properties `name` and `age`, and a data variable `person` of type `Person`, the
following are valid method names:


```groovy
def "#person is #person.age years old"() { // property access
def "#person.name.toUpperCase()"() { // zero-arg method call
```


Non-string values (like `#person` above) are converted to Strings according to Groovy semantics.


The following are invalid method names:


```groovy
def "#person.name.split(' ')[1]" {  // cannot have method arguments
def "#person.age / 2" {  // cannot use operators
```


If necessary, additional data variables can be introduced to hold more complex expressions:


```groovy
def "#lastName"() {
  ...
  where:
  person << [new Person(age: 14, name: 'Phil Cole')]
  lastName = person.name.split(' ')[1]
}
```


Additionally, to the data variables the tokens `#featureName` and `#iterationIndex` are supported.
The former does not make much sense inside an actual feature name, but there are two other places
where an unroll-pattern can be defined, where it is more useful.


```groovy
def "#person is #person.age years old [#iterationIndex]"() {
```


will be reported as


```
╷
└─ Spock ✔
   └─ PersonSpec ✔
      └─ #person.name is #person.age years old [#iterationIndex] ✔
         ├─ Fred is 38 years old [0] ✔
         ├─ Wilma is 36 years old [1] ✔
         └─ Pebbles is 5 years old [2] ✔
```


Alternatively, to specifying the unroll-pattern as method name, it can be given as parameter
to the `@Unroll` annotation which takes precedence over the method name:


```groovy
@Unroll("#featureName[#iterationIndex] (#person.name is #person.age years old)")
def "person age should be calculated properly"() {
// ...
```


will be reported as


```
╷
└─ Spock ✔
   └─ PersonSpec ✔
      └─ person age should be calculated properly ✔
         ├─ person age should be calculated properly[0] (Fred is 38 years old) ✔
         ├─ person age should be calculated properly[1] (Wilma is 36 years old) ✔
         └─ person age should be calculated properly[2] (Pebbles is 5 years old) ✔
```


The advantage is, that you can have a descriptive method name for the whole feature, while having a separate template for each iteration.
Furthermore, the feature method name is not filled with placeholders and thus better readable.


If neither a parameter to the annotation is given, nor the method name contains a `#`,
the [configuration file](extensions.md#spock-configuration-file) setting `defaultPattern`
in the `unroll` section is inspected. If it is set to a non-`null`
string, this value is used as unroll-pattern. This could for example be set to


- `#featureName` to have all iterations reported with the same name, or
- `#featureName[#iterationIndex]` to have a simply indexed iteration name, or
- `#iterationName` if you make sure that in each data-driven feature you also set
a data variable called `iterationName` that is then used for reporting


### Special Tokens

This is the complete list of special tokens:


- `#featureName` is the name of the feature (mostly useful for the `defaultPattern` setting)
- `#iterationIndex` is the current iteration index
- `#dataVariables` lists all data variables for this iteration, e.g. `x: 1, y: 2, z: 3`
- `#dataVariablesWithIndex` the same as `#dataVariables` but with an index at the end, e.g. `x: 1, y: 2, z: 3, #0`


### Configuration

**Set Default Unroll-Pattern**

```groovy
unroll {
    defaultPattern '#featureName[#iterationIndex]'
}
```


If none of the three described ways is used to set a custom unroll-pattern, by default
the feature name is used, suffixed with all data variable names and their values and
finally the iteration index, so the result will be for example
`my feature [x: 1, y: 2, z: 3, #0]`.


If there is an error in an unroll expression, for example typo in variable name, exception during
evaluation of a property or method in the expression and so on, the test will fail. This is not
true for the automatic fall back rendering of the data variables if there is no unroll-pattern
set in any way, this will never fail the test, no matter what happens.


The failing of test with errors in the unroll expression can be disabled by setting the
[configuration file](extensions.md#spock-configuration-file) setting `validateExpressions`
in the `unroll` section to `false`. If this is done and an error happens, the erroneous expression
`#foo.bar` will be substituted by `#Error:foo.bar`.


**Disable Unroll-pattern Expression Asserting**

```groovy
unroll {
    validateExpressions false
}
```


Some reporting frameworks, or IDEs support proper tree based reporting.
For these cases it might be desirable to omit the feature name from the iteration reporting.


**Disable repetition of feature name in iterations**

```groovy
unroll {
    includeFeatureNameForIterations false
}
```


With `includeFeatureNameForIterations true`


```
╷
└─ Spock ✔
   └─ ASpec ✔
      └─ really long and informative test name that doesn't have to be repeated ✔
         ├─ really long and informative test name that doesn't have to be repeated [x: 1, y: a, #0] ✔
         ├─ really long and informative test name that doesn't have to be repeated [x: 2, y: b, #1] ✔
         └─ really long and informative test name that doesn't have to be repeated [x: 3, y: c, #2] ✔
```


**With `includeFeatureNameForIterations false`**

```
╷
└─ Spock ✔
   └─ ASpec ✔
      └─ really long and informative test name that doesn't have to be repeated ✔
         ├─ x: 1, y: a, #0 ✔
         ├─ x: 2, y: b, #1 ✔
         └─ x: 3, y: c, #2 ✔
```


> [!NOTE]
> The same can be achieved for individual features by using `@Unroll('#dataVariablesWithIndex')`.

