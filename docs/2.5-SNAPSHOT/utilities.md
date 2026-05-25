# Utilities

## Testing Time with `MutableClock`

When working with dates or time we often have the problem of writing stable tests.
Java only provides a `FixedClock` for testing.
However, often time related code has to deal with the change of time,
so a fixed clock is not enough or makes the test harder to follow.


The prerequisite for using both `FixedClock` and Spocks `MutableClock` is that the production code,
actually uses a configurable `Clock` and not just the parameterless `Instant.now()`
or the corresponding methods in the other `java.time.*` classes.


### Example

**Class under Test**

```groovy
public class AgeFilter implements Predicate<LocalDate> {
  private final Clock clock;
  private final int age;

  public AgeFilter(Clock clock, int age) {                                       // <1>
    this.clock = clock;
    this.age = age;
  }

  @Override
  public boolean test(LocalDate date) {
    return Period.between(date, LocalDate.now(clock)).getYears() >= age;         // <2>
  }
}
```


1. `Clock` is injected via constructor
2. `Clock` is used to get the current date


**Test**

```groovy
def "AgeFilter reacts to time"() {
  given:
  ZonedDateTime defaultTime = ZonedDateTime.of(2018, 6, 5, 0, 0, 0, 0, ZoneId.of('UTC'))
  MutableClock clock = new MutableClock(defaultTime)                                       // <1>
  AgeFilter ageFilter = new AgeFilter(clock,18)                                            // <2>

  LocalDate birthday = defaultTime.minusYears(18).plusDays(1).toLocalDate()

  expect:
  !ageFilter.test(birthday)                                                                // <3>

  when:
  clock + Duration.ofDays(1)                                                               // <4>

  then:
  ageFilter.test(birthday)                                                                 // <5>
}
```


1. `MutableClock` created with a well known time
2. `Clock` is injected via constructor
3. `age` is less than `18` so the result is `false`
4. the clock is advanced by one day
5. `age` is equal to `18` so the result is `true`


There are many more ways to modify `MutableClock` just have a look at the JavaDocs, or the test code `spock.util.time.MutableClockSpec`.


## Collection Conditions

Sometimes, you want to assert the elements of a collection regardless of their order.
The Groovy way to do this is to cast both to `Set`, i.e. `x as Set == [1, 2, 3] as Set`.
While this works, it is very noisy.


Since Spock 2.1 you can use two new conditions:


- `x =~ [1, 2, 3]` is a lenient match, i.e., checking that x contains at least one instance of every item in the list (same semantics as casting both to `Set` before comparing).
- `x ==~ [1, 2, 3, 3]` is a strict match, i.e., checking that x contains exactly the items in the list regardless of their order (using Hamcrest’s `containsInAnyOrder` under the hood).


> [!NOTE]
> This is a Spock feature, not a Groovy feature. So it only works where Spock treats an expression as a [condition](spock_primer.md#implicit-and-explicit-conditions).


### Lenient Match

```groovy
def x = [2, 2, 1, 3, 3]
assert x =~ [4, 1, 2]
```


```
x =~ [4, 1, 2]
| |
| false
| 2 differences (66% similarity, 1 missing, 1 extra)
| missing: [4]
| extra: [3]
[2, 1, 3]
```


### Strict Match

```groovy
def x = [2, 2, 1, 3, 3]
assert x ==~ [4, 1, 2]
```


```
x ==~ [4, 1, 2]
| |
| false
[2, 2, 1, 3, 3]

Expected: iterable with items [<4>, <1>, <2>] in any order
     but: not matched: <2>
```


> [!NOTE]
> Both operands must either be `Iterable` or an array for this to work.
> Otherwise, it will be treated like the standard groovy [find operator](https://groovy-lang.org/operators.html#_find_operator) or [match operators](https://groovy-lang.org/operators.html#_match_operator).


## Interact with the file system using `FileSystemFixture`

In integration tests you often have to prepare the file system for a test.
For trivial cases like creating a single temp directory, you can use the [@TempDir](extensions.md#temp-dir) extension directly.
However, for more complex cases like creating a directory tree `FileSystemFixture` offers convenience and better readability.


### Examples

**Creating a directory tree**

```groovy
@TempDir
FileSystemFixture fsFixture

def "FileSystemFixture can create a directory structure"() {
  when:
  fsFixture.create {
    dir('src') {
      dir('main') {
        dir('groovy') {
          file('HelloWorld.java') << 'println "Hello World"'
        }
      }
      dir('test/resources') {
        file('META-INF/MANIFEST.MF') << 'bogus entry'
        copyFromClasspath('/org/spockframework/smoke/extension/SampleFile.txt')
      }
    }
  }

  then:
  Files.isDirectory(fsFixture.resolve('src/main/groovy'))
  Files.isDirectory(fsFixture.resolve('src/test/resources/META-INF'))
  fsFixture.resolve('src/main/groovy/HelloWorld.java').text == 'println "Hello World"'
  fsFixture.resolve('src/test/resources/META-INF/MANIFEST.MF').text == 'bogus entry'
  fsFixture.resolve('src/test/resources/SampleFile.txt').text == 'HelloWorld\n'
}
```


> [!TIP]
> To get the nice Groovy methods for `Path`, you need to add a dependency on `groovy-nio`.


## Capture Values for Assertions with `old()`

It can be helpful to know the old value of an expression before the `when:` was executed.
This allows you to compare the changes made by a `when:` block.


You can capture the old value of an expression with `old(<expr>)` in a `then:` block,
which will return the value of the `<expr>` before the previous `when:` block is executed.


The usage of the `old()` method makes a test less fragile, because you can assert the difference,
which was made by the `when:` block.


### Example

**Using `old()` to capture the old value of `x`**

```groovy
def "Usage of the old() method"() {
  given:
  def x = 0

  when:
  x++

  then:
  x == old(x) + 1
}
```

