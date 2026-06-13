# Release Notes

## 2.5 (tbd)

### Enhancements

- Add support for `final` local variables in `where:` blocks, declared at their beginning and evaluated once per feature, scoped to the where-block [#138](https://github.com/spockframework/spock/issues/138)
- Improve `TooManyInvocationsError` now reports unsatisfied interactions with argument mismatch details, making it easier to diagnose why invocations didn’t match expected interactions [#2315](https://github.com/spockframework/spock/pull/2315)


### Misc

- Fix argument mismatch descriptions for varargs methods by expanding varargs instead of reporting `<too few arguments>` [#2315](https://github.com/spockframework/spock/pull/2315)
- Fix Pattern flags being dropped when `java.util.regex.Pattern` instances are used in Spock regex conditions [#2298](https://github.com/spockframework/spock/issues/2298)
- Fix `MockitoMockMaker` throws NPE on null object [#2337](https://github.com/spockframework/spock/issues/2337)


### Breaking Changes

- Mock/Stub checks on `Comparable<T>` with `T` being something other than `Object` now compare using the java identity hash code instead of always being equal [#2352](https://github.com/spockframework/spock/issues/2352)


## 2.4 (2025-12-11)

*This is a summary of the highlights of the milestone releases*


### Highlights

- Add support for Groovy 5.0 [#2196](https://github.com/spockframework/spock/issues/2196)
- Add support for combining two or more data providers using Cartesian product [#1062](https://github.com/spockframework/spock/issues/1062)
- Add support for a `filter` block after a `where` block to filter out unwanted iterations [#1927](https://github.com/spockframework/spock/pull/1927)
- Add [`IBlockListener`](extensions.md#block-listener) extension point to listen to block execution events within feature methods [#1575](https://github.com/spockframework/spock/pull/1575)
- Add support for defining condition blocks with implicit assertions in helper methods annotated with `@Verify` or `@VerifyAll` [#2112](https://github.com/spockframework/spock/pull/2112)
- Add support for pluggable [mock makers](extensions.md#mock-makers) loaded via ServiceLoader [#1746](https://github.com/spockframework/spock/pull/1746)
  - This allows external libraries to contribute mocking logic to Spock and use the same API for the users
  - You can select the used mock maker during mock creation: `Mock(mockMaker:MockMakers.byteBuddy)`
- Add [mockito](extensions.md#mock-makers-mockito) mock maker [#1753](https://github.com/spockframework/spock/pull/1753) which supports:
  - Mocking of final classes and final methods
  - Mocking of static methods
  - Mocking of classes and interface from different classloaders [#1878](https://github.com/spockframework/spock/pull/1878)
  - Requires `org.mockito:mockito-core` >= 4.11 in the test class path
- Add support for mocking of static methods also for Java code with the new API `SpyStatic()` [#1756](https://github.com/spockframework/spock/pull/1756)
  - The [static mock methods](interaction_based_testing.md#static-mocks) will delegate the creation to the mock makers
- Add [verifyEach](spock_primer.md#verify-each) method to perform assertions on each element of an `Iterable` [#1887](https://github.com/spockframework/spock/pull/1887), [#2043](https://github.com/spockframework/spock/pull/2043)
- Add [annotation extensions for parameters](extensions.md#parameter-injection) [#1599](https://github.com/spockframework/spock/pull/1599)
- Add support for [keeping state in extensions](extensions.md#extension-store) [#1692](https://github.com/spockframework/spock/pull/1692)
- Add [feature-scoped interceptors](extensions.md#spock-interceptors) [#1844](https://github.com/spockframework/spock/pull/1844)
- Add `@Snapshot` extension for [snapshot testing](extensions.md#snapshot-testing) [#1873](https://github.com/spockframework/spock/pull/1873)
- Add `!!` as [opt-out operator for assertions](spock_primer.md#opt-out-of-condition-handling) [#1532](https://github.com/spockframework/spock/pull/1532)


### Breaking Changes

- *This affects users of the `@Snapshot` extension, only if you were using the snapshotter in parent specification classes.*
`@Snapshot` used to look up snapshots in directories named after the class containing feature methods. Now, the snapshots will be loaded from directories named after the bottom class in the specification hierarchy. The motivation of the change is to allow users to define features in base specification classes, but overwrite expected snapshots per child specification.
[#2112](https://github.com/spockframework/spock/pull/2112)
- *Most users will probably be unaffected by this change as it only becomes relevant in a multithreaded situation where multiple threads do interaction invocations that care about shared state.*
Calculated responses for interactions (`>> { … }`) previously were all executed synchronized on the respective mock controller instance, so could safely mutate shared state to a certain degree, even if the invocations were happening
on different threads.
This also caused that one response calculation could not wait on something happening in another response calculation, as they were all executed sequentially due to the synchronization.
Starting with this release, the response calculations are no longer happening synchronized.
If you depend on shared state in such calculations, you now have to make sure yourself, that this is done in a thread-safe manner.
[#1910](https://github.com/spockframework/spock/pull/1910)
- *This should not affect most users, only if you were subclassing `SingleResponseGenerator` and doing unusual things.*
`SingleResponseGenerator#isAtEndOfCycle` is now `final` and `SingleResponseGenerator#doRespond` is now `protected`.
When subclassing `SingleResponseGenerator` it does not make sense to override the first method, and it does not make sense to call the second method from somewhere else.
[#1910](https://github.com/spockframework/spock/pull/1910)
- The default Groovy method `.with {}` no longer has the Spock special behavior of treating it as a condition block.
This will break tests using `.with {}` in assertions.
Use the Spock `with(yourObject) {}` instead of `yourObject.with {}` or prefix it `!!` to fix your test.
- Calling `old(…)` with multiple arguments is now a compilation error. Previously the additional arguments were simply ignored.
- Creating `GroovyMock`/`GroovyStub`/`GroovySpy` for an already mocked type will now fail.
- Creating a global `GroovyMock`/`GroovyStub`/`GroovySpy` when [parallel execution](parallel_execution.md#parallel-execution) is enabled,
will now require that the spec is annotated with [@Isolated](parallel_execution.md#isolated-execution) or `@ResourceLock(org.spockframework.runtime.model.parallel.Resources.META_CLASS_REGISTRY)`. See [Global mocks and parallel execution](interaction_based_testing.md#global-mocks-parallel-execution) [#1848](https://github.com/spockframework/spock/pull/1848)
- `@TempDir` `spock.tempDir.keep` has been replaced by `spock.tempdir.cleanup`. See [TempDir Cleanup](extensions.md#temp-dir-cleanup) [#1525](https://github.com/spockframework/spock/pull/1525)


### Misc

- Fix handling of condition method calls and Groovy `.with {}` method [#2162](https://github.com/spockframework/spock/pull/2162)
  - This will break tests using `.with {}` in assertions which relied on the bug in the past [#2269](https://github.com/spockframework/spock/issues/2269)
  - Use the Spock `with(yourObject) {}` instead of `yourObject.with {}` or prefix it `!!` to fix your test
- Fix module testing not working due to call to JUnit internal API [#2187](https://github.com/spockframework/spock/pull/2187)
  - This also fixes the usage of Spock with JUnit 6 in OSGi environments


Thanks to all the contributors to this release: Andreas Turban, Björn Kautler, Choosechee, Christoph Loy, Leonard Brünings, Thanos Tsiamis, Marcin Zajączkowski, Pavlo Shevchenko, Goooler, Jérôme Prinet, jochenberger, Michał Wiśniewski, Nelson Osacky, OhioDschungel6, RahulGautamSingh, Said Boudjelda, soosue, Tasuku Nakagawa


## 2.4-M7 (2025-11-23)

### Highlights

- Add support for Groovy 5.0 [#2196](https://github.com/spockframework/spock/issues/2196)


### Misc

- Improve Spock is also tested to run correctly on Java 25 LTS [#2212](https://github.com/spockframework/spock/pull/2212)
- Fix handling of `@Verify` and `@VerifyAll` [#2150](https://github.com/spockframework/spock/issues/2150)
- Fix handling of condition method calls [#2162](https://github.com/spockframework/spock/pull/2162)
- Fix vararg handling in `SpyStatic` [#2161](https://github.com/spockframework/spock/issues/2161)
- Fix incompatibility with JUnit 6 in OSGi environment [#2231](https://github.com/spockframework/spock/issues/2231)
- Fix OSGi metadata by pinning the `Require-Capability:osgi.ee=JavaSE` to Java `8` [#2233](https://github.com/spockframework/spock/pull/2233)
- Fix NPE in `SpecRunHistory.sortFeatures` when duration is missing [#2234](https://github.com/spockframework/spock/issues/2234)
- Fix `Retry` extension does not mesh with `TestAbortedException` and `PendingFeature` [#1863](https://github.com/spockframework/spock/issues/1863)
- Fix display of caught exceptions within `verifyEach` blocks [#2163](https://github.com/spockframework/spock/pull/2163)
- Fix extensions that call `invocation.proceed()` multiple times like `@Retry` does [#1862](https://github.com/spockframework/spock/issues/1862)
- Fix setting the whole argument array of an invocation [#2240](https://github.com/spockframework/spock/pull/2240)
- Fix `SpyStatic()` with an interaction closure throws NullPointerException [#2254](https://github.com/spockframework/spock/pull/2254)


Thanks to all the contributors to this release: Andreas Turban, Björn Kautler, Christoph Loy, Leonard Brünings, Thanos Tsiamis


## 2.4-M6 (2025-04-15)

### Highlights

- Add support for defining condition blocks with implicit assertions in helper methods annotated with `@Verify` or `@VerifyAll` [#2112](https://github.com/spockframework/spock/pull/2112)


### Breaking Changes

- *This affects users of the `@Snapshot` extension, only if you were using the snapshotter in parent specification classes.*
`@Snapshot` used to look up snapshots in directories named after the class containing feature methods. Now, the snapshots will be loaded from directories named after the bottom class in the specification hierarchy. The motivation of the change is to allow users to define features in base specification classes, but overwrite expected snapshots per child specification.
[#2112](https://github.com/spockframework/spock/pull/2112)


### Misc

- Improve publish module-alignment metadata [#2082](https://github.com/spockframework/spock/pull/2082)
- Improve render multidimensional arrays in comparisons and invocation matchers [#2107](https://github.com/spockframework/spock/pull/2107)
- Improve replace unnecessary reflection [#2115](https://github.com/spockframework/spock/pull/2115)
- Fix ExtensionException in OSGi environment for global extension [#2076](https://github.com/spockframework/spock/issues/2076)
  - This issue was introduced with [#1995](https://github.com/spockframework/spock/pull/1995)
- Fix `@RestoreSystemProperties` not restoring state between iterations of a data-driven feature [#2104](https://github.com/spockframework/spock/issues/2104)
- Fix `VerifyError: Stack map does not match the one at exception handler` introduced in 2.4-M5 [#2080](https://github.com/spockframework/spock/issues/2080)
- Fix throw `SpockMultipleFailuresError` instead of a generic `MultipleFailuresError` in case of multiple failed assertions [#2112](https://github.com/spockframework/spock/pull/2112)
- Fix cross-multiplication of multi-assignment data providers [#2078](https://github.com/spockframework/spock/pull/2078)
- Fix using the same previous data table column multiple times in the same cell [#2084](https://github.com/spockframework/spock/pull/2084)
- Fix unintuitive behavior by removing the optimization which data provider is remembered in a multiplication [#2119](https://github.com/spockframework/spock/pull/2119)
- Fix several bugs in cross-multiplication implementation [#2123](https://github.com/spockframework/spock/pull/2123)
- Fix filter blocks with shared fields and derived data variables [#2088](https://github.com/spockframework/spock/pull/2088)
- Fix combined labels with comments being ignored [#2121](https://github.com/spockframework/spock/pull/2121)
- Fix boxed Boolean `is` getter methods not properly mocked in Groovy ⇐ 3 [#2131](https://github.com/spockframework/spock/issues/2131)


Thanks to all the contributors to this release: Andreas Turban, Björn Kautler, Christoph Loy, Marcin Zajączkowski, Pavlo Shevchenko


## 2.4-M5 (2025-01-07)

### Highlights

- Add support for combining two or more data providers using Cartesian product [#1062](https://github.com/spockframework/spock/issues/1062)
- Add support for a `filter` block after a `where` block to filter out unwanted iterations [#1927](https://github.com/spockframework/spock/pull/1927)
- Add [`IBlockListener`](extensions.md#block-listener) extension point to listen to block execution events within feature methods [#1575](https://github.com/spockframework/spock/pull/1575)


### Misc

- Add `globalTimeout` to the  `@Timeout` extension to apply a timeout to all features in a specification, configurable via the Spock configuration file [#1986](https://github.com/spockframework/spock/pull/1986)
- Add new [`IDefaultValueProviderExtension`](extensions.md#default-value-provider) extension point to add support for special classes in the Stub’s default `EmptyOrDummyResponse` [#1994](https://github.com/spockframework/spock/pull/1994)
- Add support for Groovy-4-style range expressions [#1956](https://github.com/spockframework/spock/issues/1956)
- Add `IStatelessAnnotationDrivenExtension` to allow a single extension instance to be reused across all specifications [#2055](https://github.com/spockframework/spock/pull/2055)
  - Built-in extensions have been updated to use this new interface where applicable
- Add new well-known versions to `Jvm` helper to support versions up to `29` [#2057](https://github.com/spockframework/spock/pull/2057)
- Add best-effort error reporting for interactions on final methods when using the `byte-buddy` mock maker [#2039](https://github.com/spockframework/spock/issues/2039)
- Add support for `@FailsWith` to assert an exception message [#2039](https://github.com/spockframework/spock/issues/2039)
- Add support for accessing the `IStore` via `ISpecificationContext` [#2064](https://github.com/spockframework/spock/pull/2064)
- Add support for ContextClassLoader when loading optional classes via `ReflectionUtil` [#1995](https://github.com/spockframework/spock/pull/1995)
  - This enables the loading of optional classes in, e.g., OSGi environments
- Improve `@Timeout` extension will now use virtual threads if available [#1986](https://github.com/spockframework/spock/pull/1986)
- Improve mock argument matching; types constraints or arguments in interactions can now handle primitive types like `_ as int` [#1974](https://github.com/spockframework/spock/issues/1974)
- Improve `verifyEach` to accept an optional second index parameter for the assertion block closure [#2043](https://github.com/spockframework/spock/pull/2043)
- Improve size of data providers is no longer calculated multiple times but only once [#2032](https://github.com/spockframework/spock/pull/2032)
- Improve documentation about data providers and `size()` calls [#2022](https://github.com/spockframework/spock/issues/2022)
- Improve `EmbeddedSpecRunner` and `EmbeddedSpecCompiler` now support the construction with a custom `ClassLoader` [#1988](https://github.com/spockframework/spock/pull/1988)
  - This allows the use of these classes in an OSGi environment, where the class imports in the embedded spec are not visible to the Spock OSGi bundle ClassLoader
- Fix a mocking issue with the ByteBuddy MockMaker when using multiple classloaders in Java 21 [#2017](https://github.com/spockframework/spock/issues/2017)
- Fix the mocking of final classes via `@SpringBean` and `@SpringSpy` [#1960](https://github.com/spockframework/spock/issues/1960)
- Fix exception when using `@RepeatUntilFailure` with a data provider with unknown iteration amount [#2031](https://github.com/spockframework/spock/pull/2031)
- Fix compile error with single explicit assert in switch expression branch [#1845](https://github.com/spockframework/spock/issues/1845)


Thanks to all the contributors to this release: Andreas Turban, Björn Kautler, Christoph Loy, Marcin Zajączkowski


## 2.4-M4 (2024-03-21)

- Fix nested regex finding in conditions [#1931](https://github.com/spockframework/spock/pull/1931)
  - Fixes [#1930](https://github.com/spockframework/spock/issues/1930) a regression introduced in M2 by [#1921](https://github.com/spockframework/spock/pull/1921)


Thanks to all the contributors to this release: Björn Kautler,


## 2.4-M3 (2024-03-21)

### Breaking Changes

- *Most users will probably be unaffected by this change as it only becomes relevant in a multithreaded situation where multiple threads do interaction invocations that care about shared state.*
Calculated responses for interactions (`>> { … }`) previously were all executed synchronized on the respective mock controller instance, so could safely mutate shared state to a certain degree, even if the invocations were happening
on different threads.
This also caused that one response calculation could not wait on something happening in another response calculation, as they were all executed sequentially due to the synchronization.
Starting with this release, the response calculations are no longer happening synchronized.
If you depend on shared state in such calculations, you now have to make sure yourself, that this is done in a thread-safe manner.
[#1910](https://github.com/spockframework/spock/pull/1910)
- *This should not affect most users, only if you were subclassing `SingleResponseGenerator` and doing unusual things.*
`SingleResponseGenerator#isAtEndOfCycle` is now `final` and `SingleResponseGenerator#doRespond` is now `protected`.
When subclassing `SingleResponseGenerator` it does not make sense to override the first method, and it does not make sense to call the second method from somewhere else.
[#1910](https://github.com/spockframework/spock/pull/1910)


### Misc

- Add option to create Groovy spies with existing instances [#1825](https://github.com/spockframework/spock/pull/1825)
- Improve Spock’s documentation by automatically linking source snippets in the docs to the code [#1904](https://github.com/spockframework/spock/pull/1904)
- Improve `@Retry` extension parallel-safeness [#1701](https://github.com/spockframework/spock/pull/1701)
- Improve `@RepeatUntilFailure` by allowing multiple annotations in the same specification [#1912](https://github.com/spockframework/spock/pull/1912)
- Improve collection matchers by supporting them in nested complex assertions [#1921](https://github.com/spockframework/spock/pull/1921)
- Improve stacktrace filtering by also handling suppressed exceptions [#1923](https://github.com/spockframework/spock/pull/1923)
- Fix possible deadlock when blocking in mock response generators [#1910](https://github.com/spockframework/spock/pull/1910)
  - Fix fallout of [#1885](https://github.com/spockframework/spock/pull/1885) introduced in M2
  - This actually fixes the issues: [#583](https://github.com/spockframework/spock/issues/583), [#1882](https://github.com/spockframework/spock/issues/1882), [#1899](https://github.com/spockframework/spock/issues/1899)
- Fix possible `StackOverflowError` when filtering exception cause loops [#1922](https://github.com/spockframework/spock/pull/1922)
- Fix NullPointerException after exception in data provider [#1925](https://github.com/spockframework/spock/pull/1925)


Thanks to all the contributors to this release: Andreas Turban, Björn Kautler, Marcin Zajączkowski


## 2.4-M2 (2024-02-26)

### Highlights

- Add support for pluggable [mock makers](extensions.md#mock-makers) loaded via ServiceLoader [#1746](https://github.com/spockframework/spock/pull/1746)
  - This allows external libraries to contribute mocking logic to Spock and use the same API for the users
  - You can select the used mock maker during mock creation: `Mock(mockMaker:MockMakers.byteBuddy)`
- Add [mockito](extensions.md#mock-makers-mockito) mock maker [#1753](https://github.com/spockframework/spock/pull/1753) which supports:
  - Mocking of final classes and final methods
  - Mocking of static methods
  - Mocking of classes and interface from different classloaders [#1878](https://github.com/spockframework/spock/pull/1878)
  - Requires `org.mockito:mockito-core` >= 4.11 in the test class path
- Add support for mocking of static methods also for Java code with the new API `SpyStatic()` [#1756](https://github.com/spockframework/spock/pull/1756)
  - The [static mock methods](interaction_based_testing.md#static-mocks) will delegate the creation to the mock makers
- Add [verifyEach](spock_primer.md#verify-each) method to perform assertions on each element of an `Iterable` [#1887](https://github.com/spockframework/spock/pull/1887)
- Add [annotation extensions for parameters](extensions.md#parameter-injection) [#1599](https://github.com/spockframework/spock/pull/1599)
- Add support for [keeping state in extensions](extensions.md#extension-store) [#1692](https://github.com/spockframework/spock/pull/1692)
- Add [feature-scoped interceptors](extensions.md#spock-interceptors) [#1844](https://github.com/spockframework/spock/pull/1844)
- Add `@Snapshot` extension for [snapshot testing](extensions.md#snapshot-testing) [#1873](https://github.com/spockframework/spock/pull/1873)
- Add `!!` as [opt-out operator for assertions](spock_primer.md#opt-out-of-condition-handling) [#1532](https://github.com/spockframework/spock/pull/1532)


### Breaking Changes

- Calling `old(…)` with multiple arguments is now a compilation error. Previously the additional arguments were simply ignored.
- Creating `GroovyMock`/`GroovyStub`/`GroovySpy` for an already mocked type will now fail.
- Creating a global `GroovyMock`/`GroovyStub`/`GroovySpy` when [parallel execution](parallel_execution.md#parallel-execution) is enabled,
will now require that the spec is annotated with [@Isolated](parallel_execution.md#isolated-execution) or `@ResourceLock(org.spockframework.runtime.model.parallel.Resources.META_CLASS_REGISTRY)`. See [Global mocks and parallel execution](interaction_based_testing.md#global-mocks-parallel-execution) [#1848](https://github.com/spockframework/spock/pull/1848)
- `@TempDir` `spock.tempDir.keep` has been replaced by `spock.tempdir.cleanup`. See [TempDir Cleanup](extensions.md#temp-dir-cleanup) [#1525](https://github.com/spockframework/spock/pull/1525)


### Misc

- Add support for parameter injection of `@TempDir`
- Add cleanup switch for `@TempDir` [#1525](https://github.com/spockframework/spock/pull/1525)
- Add support for creation of global Groovy mocks for abstract classes [#1754](https://github.com/spockframework/spock/pull/1754)
- Add optional reason to `@ResourceLock` [#1890](https://github.com/spockframework/spock/pull/1890)
- Add optional reason to `@Execution` [#1576](https://github.com/spockframework/spock/pull/1576)
- Add `onTimeout` support for `PollingConditions` [#1853](https://github.com/spockframework/spock/pull/1853)
- Add `SpecInfo#getAll…Methods()` methods [#1770](https://github.com/spockframework/spock/pull/1770)
- Add array support to collection conditions [#1734](https://github.com/spockframework/spock/pull/1734)
- Add support for logging thread dumps when [@Timeout interrupts](extensions.md#timeout-extension) are ignored [#1658](https://github.com/spockframework/spock/pull/1658)
- Add implied features to force execution of features even when removed by a build tool [#1598](https://github.com/spockframework/spock/pull/1598)
- Improve StackTraceFilter by ignoring `java.lang.invoke.*` [#1892](https://github.com/spockframework/spock/pull/1892)
- Improve `@TempDir` field injection, now it happens before field initialization, so it can be used by other field initializers.
- Improve Spock-Compiler does not use wrapper types anymore [#1765](https://github.com/spockframework/spock/pull/1765)
- Improve reduce lock contention of the `byte-buddy` mock maker, when multiple mocks are created concurrently [#1778](https://github.com/spockframework/spock/pull/1778)
- Improve `@Use` on feature and fixture method for parallel execution [#1691](https://github.com/spockframework/spock/pull/1691)
- Improve IDE support
  - by adding a closure signature hint that derives closure argument type from variable type [#1785](https://github.com/spockframework/spock/pull/1785)
  - by adding missing closure hints for Spy(T, Closure) [#1786](https://github.com/spockframework/spock/pull/1786)
  - by adding GDSL/DSLD for ConditionalExtension’s annotations [#1808](https://github.com/spockframework/spock/pull/1808)
  - by making `spock.gdsl` fail-safe and cover some more cases [#1783](https://github.com/spockframework/spock/pull/1783)
- Improve RunContexts behavior in multithreaded executions [#1758](https://github.com/spockframework/spock/pull/1758), [#1846](https://github.com/spockframework/spock/pull/1846)
- Improve generic handling by replacing `gentyref` code with [geantyref](https://github.com/leangen/geantyref) library [#1743](https://github.com/spockframework/spock/pull/1743)
  - This is now a required dependency used by spock: `io.leangen.geantyref:geantyref:1.3.14`
- Improve handling of passing `null` to `thrown()` to be consistent with mock object creation [#1799](https://github.com/spockframework/spock/pull/1799)
- Improve `old(…)` calls by validating their argument count [#1810](https://github.com/spockframework/spock/pull/1810)
- Improve robustness of the AST transformation against missing classes in the compile classpath [#1704](https://github.com/spockframework/spock/pull/1704)
- Improve handling of `shared.` conditions in `@IgnoreIf`/`@Requires`, the condition is now checked before creating data providers [#1711](https://github.com/spockframework/spock/pull/1711)
- Fix issue with mocks of Groovy classes, where the Groovy MOP for `@Internal` methods was not honored by the `byte-buddy` mock maker [#1729](https://github.com/spockframework/spock/pull/1729)
  - This fixes multiple issues with Groovy MOP: [#1501](https://github.com/spockframework/spock/issues/1501), [#1452](https://github.com/spockframework/spock/issues/1452), [#1608](https://github.com/spockframework/spock/issues/1608) and [#1145](https://github.com/spockframework/spock/issues/1145)
- Improve support for generic return types for mocks [#1731](https://github.com/spockframework/spock/pull/1731)
  - This fixes the issues: [#520](https://github.com/spockframework/spock/issues/520), [#1163](https://github.com/spockframework/spock/issues/1163)
- Fix the lifecycle of simple features [#1675](https://github.com/spockframework/spock/pull/1675)
- Fix exception when configured `baseDir` was not existing, now `@TempDir` will create the baseDir directory if it is missing.
- Fix bad error message for collection conditions, when one of the operands is `null`
- Fix docs about initializer method interceptors [#1666](https://github.com/spockframework/spock/pull/1666)
- Fix possible deadlock, when blocking in mock response generators [#1885](https://github.com/spockframework/spock/pull/1885)
  - This fixes the issues: [#583](https://github.com/spockframework/spock/issues/583), [#1882](https://github.com/spockframework/spock/issues/1882)
- Fix SpringSpy not working with `DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD` [#1869](https://github.com/spockframework/spock/pull/1869)
- Fix null handling for collection conditions [#1858](https://github.com/spockframework/spock/pull/1858)
- Fix interceptor contexts [#1676](https://github.com/spockframework/spock/pull/1676)
- Fix properly call `IRunListener.specSkipped` and `.featureSkipped` [#1811](https://github.com/spockframework/spock/pull/1811)
- Fix defining responses for additionalInterfaces [#1730](https://github.com/spockframework/spock/pull/1730)
- Fix lost execution mode for iteration nodes of implied data-driven features [#1615](https://github.com/spockframework/spock/pull/1615)
- Fix docs how to register all possible interceptors [#1667](https://github.com/spockframework/spock/pull/1667), [#1824](https://github.com/spockframework/spock/pull/1824)
- Document `@ConditionBlock` Annotation [#1709](https://github.com/spockframework/spock/pull/1709)
- Document `old`-Method [#1708](https://github.com/spockframework/spock/pull/1708)
- Document for DetachedMockFactory [#1728](https://github.com/spockframework/spock/pull/1728)
- Clarify documentation for global Mocks [#1755](https://github.com/spockframework/spock/pull/1755)


Thanks to all the contributors to this release:  Andreas Turban, Björn Kautler, Goooler, Jérôme Prinet, Marcin Zajączkowski, Michał Wiśniewski, Nelson Osacky, Pavlo Shevchenko, RahulGautamSingh, Said Boudjelda, Tasuku Nakagawa, jochenberger, soosue


## 2.4-M1 (2022-11-30)

- Fix issues with Spring 6/Spring Boot 3 [#1541](https://github.com/spockframework/spock/pull/1541)


## 2.3 (2022-09-29)

- Add RepeatUntilFailure extension [#1522](https://github.com/spockframework/spock/pull/1522)
- Fix problem with TempDir failing when using a custom class that extends from `File` or `Path` [#1519](https://github.com/spockframework/spock/pull/1519)
- Fix issue with `Object` methods on interface spies [#1529](https://github.com/spockframework/spock/pull/1529)
- Fix equality checking of Mocks that implement `Comparable` [#1323](https://github.com/spockframework/spock/pull/1323)
- Validate uniqueness of fixture methods per specification class [#1521](https://github.com/spockframework/spock/pull/1521)


> [!NOTE]
> The changes to equality checking for `Comparable` mocks can lead to different behavior when no explicit `compareTo` interactions was defined.
> For example, Groovy will use `compareTo` to check equality when classes implement `Compareable`.
> Until now, the mock would return `0` for default invocations, which would lead to the objects being considered equal.


Thanks to all the contributors to this release: Jackson Popkin


## 2.2 (2022-08-31)

- Support for `@SpringBean` and `@SpringSpy` marked with `@Primary` [#1503](https://github.com/spockframework/spock/pull/1503)
- Fix issue that `displayName` values for the `Specification`, which were changed by extensions such as [@Title](extensions.md#title-and-narrative-extension), not being picked up. [#1509](https://github.com/spockframework/spock/pull/1509)
- Update to JUnit 5.9.0


Thanks to all the contributors to this release: Alexander Kriegisch, Marcin Zajączkowski, Jonny Carter, alopukhov, Jerome Prinet, Matthew Moss, BJ Hargrave, konradczajka


## 2.2-M3 (2022-07-15)

- Add support for `IterationSelector` to run individual iterations
- Update to JUnit 5.9.0-RC1


Thanks to all the contributors to this release: Marc Philipp


## 2.2-M2 (2022-07-15)

- Add junit-platform `TestTag` support with the [@Tag](extensions.md#test-tag-extension) extension [#1467](https://github.com/spockframework/spock/pull/1467)
- Add `IDataDriver` extension point and refactor data provider handling [#1479](https://github.com/spockframework/spock/pull/1479)
- Add [named deconstruction](data_driven_testing.md#multi-data-pipe-named) for multi variable datapipes.
This might lead to changed behavior results if the data object implements `Map` but supports both positional `getAt(int)` and named `getAt(String)` access. In earlier versions, the positional access was used, but now the named access will be used. [#1463](https://github.com/spockframework/spock/pull/1463)
- Add custom class support to `@TempDir`, you can use any class that has a single `java.io.File` or `java.nio.file.Path` as constructor parameter [#1430](https://github.com/spockframework/spock/pull/1430)
- Improve `@Stepwise` can be applied to data-driven feature methods, having the effect of executing them sequentially (even if concurrent test mode is active) and to skip subsequent iterations is one iteration fails. [#1442](https://github.com/spockframework/spock/pull/1442)
- Improve `EmbeddedSpecCompiler` by making package declaration configurable in `EmbeddedSpecCompiler`
- Improve do not evaluate feature-skipping conditions for skipped specs [#1459](https://github.com/spockframework/spock/pull/1459)
- Improve behavior when trying to run Spock with unsupported Groovy version in IDEA [#1421](https://github.com/spockframework/spock/issues/1421)
- Improve Java 17 compatibility by using new `invokeDefault` instead of reflection
- Fix compatibility with certain locales, for example `tr_TR` [#1414](https://github.com/spockframework/spock/issues/1414)
- Fix Spring 6 incompatibility [#1428](https://github.com/spockframework/spock/issues/1428)
- Fix test reporting issue with Maven, where an error on the Specification level was not visible [#1444](https://github.com/spockframework/spock/issues/1444)
- Fix context pollution in `IterationNode.around()` [#1441](https://github.com/spockframework/spock/issues/1441)
- Fix Discard unnecessary state in `ConfineMetaClassChangesInterceptor` [#1460](https://github.com/spockframework/spock/pull/1460)
- Fix make `EnableSharedInjection` public [#1472](https://github.com/spockframework/spock/pull/1472)
- Fix gradle module metadata dependencies [#1490](https://github.com/spockframework/spock/pull/1490)
- Remove runtime dependency on Jetbrains Annotations [#1468](https://github.com/spockframework/spock/pull/1468)


Thanks to all the contributors to this release: Alexander Kriegisch, Marcin Zajączkowski, Jonny Carter, alopukhov, Jerome Prinet, Matthew Moss, BJ Hargrave


## 2.2-M1 (2022-02-16)

- Add Groovy 4 support [#1382](https://github.com/spockframework/spock/pull/1382)


No other changes to the 2.1 release.


Thanks to all the contributors to this release: Marcin Zajączkowski


## 2.1 (2022-02-15)

No functional changes to 2.1-M2.


## Official Spock Logo

![Spock Logo](images/spock-main-logo.png)


The Spock Framework Project has an official logo.
Many thanks to Ayşe Altınsoy (@AltinsoyAyse) for creating the logo through many iterations.


## Misc

- Documentation fixes
- Build maintenance


Thanks to all the contributors to this release: Marc Philipp, Miles Thomason, BJ Hargrave, Marcin Zajączkowski, Lőrinc Pap, Felipe Pedrosa, Marcin Świerczyński, Benedikt Ritter, Alexander Kriegisch, Jérôme Prinet, Pin Zhang


## 2.1-M2 (2021-11-12)

- Fix issue with generated gradle module metadata that caused issues with consumers.
- Update JUnit, ASM, ByteBuddy dependencies.


## 2.1-M1 (2021-11-12)

### Highlights

- Add [collection conditions](utilities.md#collection-conditions) [#1372](https://github.com/spockframework/spock/issues/1372)
- Add support for selecting individual iterations via their unique ID (IDE support required). [#1376](https://github.com/spockframework/spock/pull/1376)


### Breaking Changes

- Add `data.` support to conditional extensions [#1360](https://github.com/spockframework/spock/issues/1360).
This replaces the  current behavior of accessing data variables without any prefix.
See [Precondition Context](extensions.md#precondition-context) for more details.


### Misc

- Add exception translation to JUnit4 Rules [#1342](https://github.com/spockframework/spock/pull/1342)
- Add option to omit feature name from iterations [#1386](https://github.com/spockframework/spock/pull/1386)
and add additional [Special Tokens](data_driven_testing.md#unroll-tokens) to unroll patterns.
- Add optional reason to `@Requires` and `@IgnoreIf` [#1362](https://github.com/spockframework/spock/pull/1362)
- Add `shared.` support to conditional extensions [#1359](https://github.com/spockframework/spock/pull/1359).
See [Precondition Context](extensions.md#precondition-context) for more details.
- Set the owner for condition closures on spec annotations [#1357](https://github.com/spockframework/spock/pull/1357)
  This allows access to static methods when `@IgnoreIf`, `@Requires` and `@PendingFeatureIf` is used on a Specification.
  
  
- Update bnd Gradle plugin to 6.0.0 (OSGI) [#1377](https://github.com/spockframework/spock/pull/1377)
- Fix selector ordering issue [#1375](https://github.com/spockframework/spock/pull/1375)
- Fix `@TempDir` not working for `@Shared` inherited fields [#1373](https://github.com/spockframework/spock/pull/1373)
- Fix JUnit rule run order [#1363](https://github.com/spockframework/spock/pull/1363)
- Prevent removal of ErrorSpecNode from execution hierarchy [#1358](https://github.com/spockframework/spock/pull/1358)
  As the ErrorSpecNode does not have children it would get removed
  when trying to select specific tests methods
  For example, gradle will report that no test were found,
  and not report the actual error.
  
  
- Fix double invocation of IRunListener.beforeSpec and IRunListener.afterSpec [#1344](https://github.com/spockframework/spock/pull/1344)
- Fix regression with multi-assignment of method call result [#1333](https://github.com/spockframework/spock/pull/1333)
- Fix Build MethodSource with the correct spec class name [#1345](https://github.com/spockframework/spock/pull/1345)
  Prior to this fix, all `SpockNode` that use a `MethodSource`
  did not use the actual test class of the discovered method, and
  instead used the declaring class. This was problematic for inherited test
  methods, since they appeared to come from the declaring class instead of
  the current test class. In addition, the Maven Surefire provider failed
  to match such methods when executing tests matching a mask (e.g., via
  `-Dtest=*MaskTest`).
  
  
- Automatically test on CI with JDK 17 (only `-groovy-3.0` variant)


Thanks to all the contributors to this release: Marc Philipp, Miles Thomason, BJ Hargrave, Marcin Zajączkowski, Lőrinc Pap, Felipe Pedrosa, Marcin Świerczyński, Benedikt Ritter


## 2.0 (2021-05-17)

### Highlights

- Spock is now a test engine based on the JUnit Platform
- Spock now supports [Parallel Execution](parallel_execution.md#parallel-execution) on the spec and feature level, with powerful tools for shared resource access.
- Support for Groovy 3
- Data driven tests are now unrolled by default with a more informative naming scheme
- Data driven tests can now declare a subset of parameters, which are injected by name instead of position.


... and many more, read the full release notes for every detail.


### Migrating from 1.x

The [Migration Guide 1.x - 2.0](migration_guide.md#migration-guide-2-0) covers the major breaking changes between 1.3 and 2.0


### Breaking Changes (from 2.0-M5)

- Remove `@ResourceLockChildren` again, as JUnit Platform 5.7.2 makes it obsolete.
With the update a `READ` lock on a parent node, does not force same thread execution on its children.
If you are one of the few that have already used `@ResourceLockChildren`, just replace it with `@ResourceLock`.


### Misc

- Fix NPE if variable is initialized using a method with the same in features with cleanup blocks or using thrown condition [#1266](https://github.com/spockframework/spock/issues/1266) [#1313](https://github.com/spockframework/spock/issues/1313)
- Fix implicit this conversion after bugfix in Groovy 3.0.8 [#1318](https://github.com/spockframework/spock/issues/1318)
- Fix extension-provided method arguments in fixture methods [#1305](https://github.com/spockframework/spock/issues/1305)
- Update `Jvm` helper to support versions up to 23 (next LTS release)


Thanks to all the contributors (all 2.0 Milestones): Björn Kautler, Marcin Zajączkowski, DQYuan, Marcin Erdmann, Alexander Kriegisch, Jasper Vandemalle, Tom Wieczorek, Josh Soref, Vaidotas Valuckas, Raymond Augé, Roman Tretiak, Camilo Jorquera, Shil Sinha, Ryan Gardner, k3v1n0x90


Special thanks goes to Marc Philipp who helped a lot with the integration of the JUnit Platform.


## 2.0-M5 (2021-03-23)

### Breaking Changes

- The `ReportLogExtension` vestiges were removed. As this extension was mostly used for an unreleased Spock module, this won’t affect many users.
If you are using a [Spock Configuration File](extensions.md#spock-configuration-file) with a `report` section,
then you must delete everything from this section except for `issueNamePrefix` and `issueUrlPrefix`.
These two properties are still supported and used by the `@Issue` extension.


### Misc

- Add support for injection into `@Shared` fields in `spock-spring` module which users can opt-in for by adding
`@EnableSharedInjection` to the specification. [#76](https://github.com/spockframework/spock/issues/76)


- Add new `displayName` via `INameable` for `SpecInfo`, `FeatureInfo`, and `IterationInfo`.
This field can be set via extensions to change the reported name.
The existing iteration `NameProvider` now also sets the `displayName` instead of the `name`.
Modifying the `name` instead of `displayName` is now considered `deprecated` for extensions.
[#1236](https://github.com/spockframework/spock/issues/1236)
- Add support for constructor injection for extensions
- Improve final field handling.
Final fields are now transformed similar to shared fields, so that we can still delay the initialization but keep them
unmodifiable to user code. [#1011](https://github.com/spockframework/spock/issues/1011)
- Improve parallel extensions to support inheritance [#1245](https://github.com/spockframework/spock/issues/1245)
- Improve PollingConditions
- Improve some AST transformation code regarding error handling
- Deprecate `AbstractGlobalExtension` and replace with `IGlobalExtension`
- Remove unnecessary try-finally construct for assertions
- Fix ErrorSpecNode throw Exception in `prepare` instead of `execute`
this fixes an issue that interceptors for `prepare`, `before`, and `around`
were still executed and any Exception they throw would hide the actual cause.
- Fix [#1294](https://github.com/spockframework/spock/issues/1294) swallowing of unrecoverable Errors
- Fix [#1260](https://github.com/spockframework/spock/issues/1260) Change InteractionRewriter to keep casting of ListExpressions intact
- Fix [#1282](https://github.com/spockframework/spock/issues/1282) Make TempDirInterceptor safe for parallel invocation of iterations
- Fix [#1267](https://github.com/spockframework/spock/issues/1267) Retry extension behavior for unrolled tests
- Fix [#1267](https://github.com/spockframework/spock/issues/1267) Retry extension behavior for unrolled tests
- Fix [#1229](https://github.com/spockframework/spock/issues/1229) `@TempDir` not working for inherited fields
- Fix [#1232](https://github.com/spockframework/spock/issues/1232) compile error for nested conditions without top-level condition
- Fix [#1256](https://github.com/spockframework/spock/issues/1256) handling of `is` as getter for boolean properties on Mocks
- Fix [#1270](https://github.com/spockframework/spock/issues/1270) handling of `is` as getter for boolean properties on GroovyMocks
- Fix `ErrorSpecNode` to re-throw `Exception` in `prepare` instead of `execute`,
this fixes an issue that interceptors for `prepare`, `before`, and `around`
were still executed and any Exception they throw would hide the actual cause.
- Fix [#1263](https://github.com/spockframework/spock/issues/1263) ExceptionAdapterExtension to also handle inherited fixture methods
- Fix [#1279](https://github.com/spockframework/spock/issues/1279) Cast data variables with type coercion to the declared parameter type


Thanks to all the contributors to this release: Marcin Erdmann, Björn Kautler, Alexander Kriegisch, Josh Soref, Vaidotas Valuckas


## 2.0-M4 (2020-11-01)

- **Added <<parallel_execution.adoc#parallel-execution,Parallel Execution>> support**
- Add a way to register `ConfigurationObject` globally without the need for a global extension.
- Annotations for local extensions can now be defined as `@Repeatable` and applied multiple times to the same target.
Spock will handle this appropriately and call the extensions `visitSpecAnnotations` method with all annotations.
If these methods are not overwritten, they forward to the usual `visitSpecAnnotation` methods once for each annotation.
- `@ConfineMetaClassChanges`, `@Issue`, `@IgnoreIf`, `@PendingFeatureIf`, `@Requires`, `@See`, `@Subject`, `@Use`, and `@UseModules` are now repeatable annotations
- `@Requires`, `@IgnoreIf` and `@PendingFeatureIf` can now access instance fields, shared fields and instance methods
by using the `instance.` qualifier inside the condition closure.
- `AbstractAnnotationDrivenExtension` is now deprecated and its logic was moved to `default` methods of
`IAnnotationDrivenExtension` which should be implemented directly now instead of extending the abstract class.
- Add `@TempDir` built-in extension
- `@PendingFeature` and `@PendingFeatureIf` can now be used together
- Fix [#1158](https://github.com/spockframework/spock/issues/1158) Fix strange bug with setter/getter handling of mocks in groovy
- Fix [#1216](https://github.com/spockframework/spock/issues/1216) perform argument coercion for `GroovyMock` method arguments
- Fix [#1169](https://github.com/spockframework/spock/issues/1169) check skipped state in Node.prepare and do nothing if already skipped
- Fix [#1200](https://github.com/spockframework/spock/issues/1200) name clashes where variables that are named like method calls destroy the method call
- Fix [#1202](https://github.com/spockframework/spock/issues/1202) NullPointerException with array initializers
- Fix [#994](https://github.com/spockframework/spock/issues/994) don’t treat nested closures in argument constraints as implicit assertions anymore
- Replace `hamcrest-core` dependency by `hamcrest`


Thanks to all the contributors to this release: Björn Kautler, Marcin Zajączkowski, DQYuan, Tom Wieczorek, Alexander Kriegisch, Jasper Vandemalle


## 2.0-M3 (2020-06-11)

### Breaking Changes

#### Sputnik Runner removed (an alternative)

In 2.0-M1 the Sputnik runner was removed and the 2.0-M2 release notes explicitly mentioned that enhancements
that extend `Sputnik` or use it as a delegate like for example the `PowerMockRunnerDelegate` will not work anymore.


This is not the full truth though, but be aware that the information in this section is just for informational purpose.
This is not a solution the Spock maintainers explicitly support or maintain. It is just mentioned as hint for those
that have no other choice right now. It is strongly recommended to instead use native solutions or integrations. If
those are not available, you might consider asking about proper integration with Spock for a proper long-term solution.
As long as this is not available, the work-around described here is at least usable as fallback and mid-term solution
as long as the JUnit team decides to maintain this legacy support module.


There is a JUnit 4 runner provided by JUnit 5 called `JUnitPlatform` that can
run any JUnit platform based tests - like Spock 2+ based tests - in a JUnit 4 environment. This is intended
for situations where an IDE, build tool, CI server, or similar does not yet natively support JUnit platform.
It is provided in the artifact `org.junit.platform:junit-platform-runner`.


This means, if you make sure your tests are launched with JUnit 4 and you use `JUnitPlatform` where you before used
`Sputnik`, all should work out properly. In case of PowerMock this means you annotate your specification with
`@RunWith(PowerMockRunner)` and `@PowerMockRunnerDelegate(JUnitPlatform)` and make sure your tests are launched
using JUnit 4.


#### Removal of JUnit 4 dependency from spock-core

Spock 2.0 from the beginning has leveraged JUnit Platform to execute tests. However, starting with 2.0-M3 `junit4.jar` is no longer a transitive dependency of `spock-core`. This might affect people using `@Before/@After/…` instead of Spock-native `setup/cleanup/…` fixture methods. To keep it work the `spock-junit4` dependency has to be added.


As a side effect of the removal, the order of the fixture methods execution has changed from:


```
beforeClass, setupSpec, before, setup, cleanup, after, setup, before, cleanup, after, cleanupSpec, afterClass
```


to:


```
setupSpec, beforeClass, setup, before, cleanup, after, setup, before, cleanup, after, cleanupSpec, afterClass
```


which should not be a problem in the majority of cases.


At the same time, using JUnit 4’s annotations is discouraged (and considered `deprecated`), the same using the other elements of JUnit 4 (as `(Class)Rule`s).


#### Reduce spock-core direct groovy dependencies

`spock-core` now only depends on `groovy.jar`. All other Groovy dependencies have been removed,
this should make dependency management a bit easier.
If you relied on other groovy dependencies transitively, you will need to add them directly.


#### Upgrade to JUnit 5.6 (and JUnit Platform 1.6)

JUnit Platform 1.6 [deprecated](https://junit.org/junit5/docs/5.6.0/release-notes/index.html#deprecations-and-breaking-changes) methods (from experimental API)  in `EngineExecutionResults` that Spock was using. To keep runtime compatibility with JUnit 5.6 and incoming 5.7 the implementation has been switched to the new methods. As a result, Spock 2.0-M3 cannot work with JUnit 5.5 and lower. The problem might only occur if a project overrides default JUnit Platform version provided by Spock.


#### New meaning of `>> _`

The meaning of `>> _` has changed from "use the default response" to "return a stubbed value" ([Docs](interaction_based_testing.md#_returning_a_default_response)).
The original behavior was only ever documented in the Javadocs and was basically the same to just omitting it.
The only use-case was chained responses `>> "a" >> _ >> "b"`,
but even here it is clearer to just use `null` or `{ callRealMethod() }` explicitly.
With the new behavior, you can have a `Mock` or `Spy` return the same value as a `Stub` would.


```groovy
subscriber.receive(_) >> _
```


#### Renamed iterationCount token

The token `#iterationCount` in unroll patterns was renamed to `#iterationIndex`.
If you use it somewhere, you have to manually change it to the new name
or the test will fail unless you disabled expression asserting,
then you will get an `#Error:iterationCount` rendering instead.


#### No access to data variables in data pipes anymore

It is not possible anymore to access any data variable from a data pipe or anything else but a previous data table
column in a data table cell. This access was partly possible, but could easily prematurely drain iterators, access
data providers sooner as expected, behaved differently depending on the concrete code construct used. All these
points are more confusing than necessary. If you want to calculate a data variable from others, you can always use
a derived data variable that has full access to all previous data variables and can also call helper methods for
more complex logic.


If you switch your tests that are fully green to use Spock 2.0 and get any `MissingPropertyException`s, you are probably hitting this change, you should then change to a derived data variable there instead of a data pipe.


If you for example had:


```groovy
where:
a << [1, 2]
b << a
```


what you want instead is:


```groovy
where:
a << [1, 2]
b = a
```


#### Assert unroll expressions by default

The system property `spock.assertUnrollExpressions` is not supported anymore.
Instead the new default behavior is equal to having this property set to `true`.
This means tests that were successful but had an `#Error:` name rendering will now fail.
It can be set back to the old pre Spock 2.0 behaviour by setting
`unroll { validateExpressions false }` in the Spock configuration file.


#### For extension developers

- `FeatureInfo#getDataVariables()` and `FeatureInfo#getParameterNames()` used to return the
same value, the parameter names, in the order of the method parameters. This can disturb some calculations like method
argument determination and so on and is plainly wrong, as some parameters could be injected by extensions like
injecting mock objects or test proxies or similar. `FeatureInfo#getDataVariables()` now only returns the actual data
variables and in the order how they are defined in the `where` block.
- Method arguments in `MethodInfo` now have a value of `MethodInfo.MISSING_ARGUMENT` if no value was set so far,
for example by some extensions or from data variables. If any of these is not replaced by some value, an exception
will be thrown at runtime.


#### Ant custom selector removed

The class `SpecClassFileSelector` that could be used to only select actual Spock specification classes when testing
and creating a test report was removed. It was the only class that required `ant` and was basically just a one-line
forward to the `SpecClassFileFinder` which is still available.


If you are still using Spock with `ant`, then you can just copy [the class from the spock source code](https://github.com/spockframework/spock/blob/03818ed010f4b4ca1136a292e214f79d518c4abe/spock-core/src/main/java/org/spockframework/buildsupport/ant/SpecClassFileSelector.java)
into your build, or you simply use a [`<scriptselector>`](https://github.com/spockframework/spock-example/blob/963fd34d1609b7025ba92502483ce31b2c6d9d0a/build.xml#L167-L176) that does this forwarding as shown in the
[Spock Example Project](https://github.com/spockframework/spock-example).


Alternatively, you can also use a naming convention to find the classes that are actual specifications and not helper
or base classes, or you live with a bit of wasted time and some meaningless entries in the test reports.


### Misc

- A new `MutableClock` utility class to support time related testing, see [docs](utilities.md#mutable-clock).
- Defining interactions on property getters within Mock instantiation closures or `with` closures works now without
the need to explicitly qualify the property access with `it.`:
  ```groovy
  Foo foo = Stub {
      bar >> 'my stubbed property'
  }
  ```
  
  
- A sequence of two or more underscores can be used to separate multiple data tables in one `where` block.
- Type casts in conditions are now properly carried over to properly disambiguate method calls.
For more information see issue [#1022](https://github.com/spockframework/spock/issues/1022).
- Data tables now support any amount of semicolons to separate data table columns as alternative
to pipes and double pipes, but cannot be mixed with them in one table:
  ```groovy
  where:
  a ; b ;; c
  1 ; 2 ;; 3
  ```
  
  


- The default unroll pattern changed from the rather generic `#featureName[#iterationIndex]` to a more fancy
version that lists all data variables and their values additionally to the feature name and iteration index.
If you prefer to retain the old behaviour, you can set the setting
`unroll { defaultPattern '#featureName[#iterationIndex]' }` in the Spock configuration file
and you will get the same result as previously.
- If neither a parameter to the `@Unroll` annotation is given, nor the method name contains a `#`,
now the configuration file setting `unroll { defaultPattern }` is inspected. If it is set to a non-`null` string,
this value is used as unroll pattern.
- `IterationInfo` now has a property for the `iterationIndex` and one for a map of data variable names to values.
This is typically not interesting for the average user, but might be helpful for authors of Spock extensions.
- `constructorArgs` now properly transport type cast information to the constructor selection,
for example to disambiguate multiple candidate constructors:
  ```groovy
  Spy(constructorArgs: [null as String, (Pattern) null])
  ```
  
  
- Accessing previous data table columns was broken in some cases, now it should work properly,
even cross-table and without being disturbed by previous derived data variables.
- The order of parameters in a data-driven feature does no longer have to be identical to
the declaration order in the `where` block. Data variables are now injected by name.
- Data driven features do no longer have the requirement that either none or all data variables are declared as
parameters and that all parameters are also data variables. Now you can either declare none, some, or all data
variables as parameters and also have additional method parameters that are no data variables.
Those additional parameters must be provided by some Spock extension though. If they are not set at execution time,
an exception will be thrown.
- Condition closures used in `@Requires`, `@IgnoreIf` and `@PendingFeatureIf` now also get the context passed
as argument, so you can use this, typed as `org.spockframework.runtime.extension.builtin.PreconditionContext` to
enable IDE support like code completion:
  ```groovy
  @IgnoreIf({ PreconditionContext it -> it.os.windows })
  def "I'll run everywhere but on Windows"() { ... }
  ```
  
  
- Execution with an unsupported Groovy version can be allowed with `-Dspock.iKnowWhatImDoing.disableGroovyVersionCheck=true` [#1164](https://github.com/spockframework/spock/pull/1164)
- Relax maximum allowed Groovy version in snapshot builds [#1108](https://github.com/spockframework/spock/pull/1108)
- Upgrade Groovy to 2.5.12 (improved Java 14+ support) and 3.0.4 (fixes [#1127](https://github.com/spockframework/spock/issues/1127))
- Upgrade JUnit 4 to 4.13 (in `spock-junit4`)
- Upgrade Hamcrest to 2.2 (from 1.3 provided previously by `junit4.jar`)
- Derived data variables (assignments in `where` blocks) now also support multi-assignment syntax, including ignoring
some values with the wildcard expression.
  ```groovy
  (a, b, _, c) = row
  ```
  
  
- Multi-variable data pipes now also support nesting.
  ```groovy
  [a, [_, b]] << [
    [1, [5, 1]],
    [2, [5, 2]]
  ]
  ```
  
  
- The condition closures for `@IgnoreIf`, `@Requires` and `@PendingFeatureIf` can now access data variables
if applied to a data driven feature. This has further implications that are documented in the respective
documentation parts and JavaDocs.


- Data driven features are now unrolled by default. `@Unroll` can still be used to specify a custom naming pattern.
A simple `@Unroll` without argument is not needed anymore except when undoing a spec-level `@Rollup` annotation
or if unrolling by default is disabled, so any simple `@Unroll` annotations can be removed from existing code. You
can verify this by looking at the test count which should not have been changed after you removed the simple
`@Unroll` annotations.
- `@Rollup` can now be used on feature and spec level to explicitly roll up any feature where the reporting of single
iterations is not wanted.
- The setting `unroll { unrollByDefault false }` in the Spock configuration file can be set to roll up all
features by default if not overwritten by explicit `@Unroll` annotations and thus reinstate the pre
Spock 2.0 behaviour.
- Updated OSGI support with using `bnd` ([#1154](https://github.com/spockframework/spock/pull/1154), [#1175](https://github.com/spockframework/spock/pull/1175))
- Fix `@PendingFeature` logic [#1103](https://github.com/spockframework/spock/pull/1103)
- Do not strip type information from arguments [#1134](https://github.com/spockframework/spock/pull/1134)
- Simplify work-around to get the reference to the current closure [#1131](https://github.com/spockframework/spock/pull/1131)
- Set source position for return statement in data provider method [#1116](https://github.com/spockframework/spock/pull/1116)
- Add `Spy(T obj, Closure interactions)` [#1115](https://github.com/spockframework/spock/pull/1115)
- Reduce number of Groovy dependencies to just groovy.jar [#1109](https://github.com/spockframework/spock/pull/1109)
- Improve `ExceptionUtil.sneakyThrow` declaration [#1077](https://github.com/spockframework/spock/pull/1077)
- Print up to 5 last mock invocations for a wrong order error [#1093](https://github.com/spockframework/spock/pull/1093)
- Fix `EmptyOrDummyResponse` returning mock instance for `Object` [#1092](https://github.com/spockframework/spock/pull/1092)
- Remove unnecessary reflection for Java 8 types in `EmptyOrDummyResponse` [#1091](https://github.com/spockframework/spock/pull/1091)
- Update old Issue annotations to point to migrated github issues [#1003](https://github.com/spockframework/spock/pull/1003)
- Test Spock with Java 14 [#1155](https://github.com/spockframework/spock/pull/1155)


Thanks to all the contributors to this release: Björn Kautler, Marcin Zajączkowski, Raymond Augé, Roman Tretiak, Camilo Jorquera, Shil Sinha


## 2.0-M2 (2020-02-10)

### Groovy-3.0 Support

The main feature of this milestone is support for Groovy 3.
To use Spock in your Groovy 3 project just select the `spock-\*-2.0-M2` artifact(s) ending with `-groovy-3.0`.


> [!NOTE]
> As Groovy 3 is not backward compatible with Groovy 2, there is a layer of abstraction in Spock to allow to build (and use) the project with both Groovy 2 and 3.
>       As a result, an extra artifact `spock-groovy2-compat` is (automatically) used in projects with Groovy 2.
>       It is **very important** to **do not mix** the `spock-*-2.x-groovy-2.5` artifacts with the `groovy-*-3.x` artifacts on a classpath.
>       This may result in weird runtime errors.


### Breaking Changes

#### Sputnik Runner removed

Although already in 2.0-M1 it wasn’t explicitly mentioned: All enhancements that either extended `Sputnik`
or used it as a delegate runner will not work anymore, e.g, `PowerMockRunnerDelegate`.


### Misc

- Add `@PendingFeatureIf` annotation ([Docs](extensions.md#_pendingfeatureif))
- Fail-fast for invalid `Stub` interactions, added new validation that catches more invalid usage of `Stub`
- Forbid spying on `Spy` instances, as this doesn’t work anyway and leads to wrong expectations ([#1029](https://github.com/spockframework/spock/issues/1029))
- Update `Jvm` helper utility to include new Java versions, removed pre 8 versions
- Update docs regarding ByteBuddy, cglib, objenesis
- Provide compatibility to both JUnit 5.5.2 and 5.6.0


Thanks to all the contributors to this release: Marcin Zajączkowski (Groovy 3 support), Björn Kautler, Ryan Gardner


## 2.0-M1 (2019-12-31)

This is the first milestone release to version 2.0. This means that we have migrated to the new
JUnit Platform and all internal tests pass. We have tried to keep the API as compatible as possible
and if you’ve only used the public spock API then there is a high possibility that all you have to
do is to update the spock version and configure gradle/maven to use the JUnit Platform.


However, this doesn’t mean that the API is finalized yet, the goal for the first milestone was just
to get it running on the new platform. The next milestones will focus on improvements like the much
requested parallel execution support.


Please try it out and report any new bugs so that we can fix them for the final 2.0 release.


### Breaking Changes

#### New JUnit Platform

Switch from JUnit 4 to the JUnit Platform.
See https://junit.org/junit5/docs/current/user-guide/#running-tests-build on how to configure
maven and gradle to use the JUnit Platform.


JUnit 4 Rules are not supported by `spock-core` anymore, however, there is a new  `spock-junit4`
module that provides best effort support to ease migration.


#### Misc

- Spock now requires at least Java 8
- All data-driven test iterations are always reported (unrolled), while `@Unroll` is not necessary anymore
it can still be used to define the name template
- `@Retry.Mode.FEATURE` didn’t work anymore and has been removed
- `spock-report` module has been removed, it was never officially released
- The `SpockReportingExtension` has been disabled until we can integrate it with JUnitPlatform
- Removed testing for `spring-2.x` as it is incompatible
- Fix some Javadocs


Special thanks goes to Marc Philipp who helped a lot with the integration of the JUnit Platform.


Thanks to all the contributors to this release: Marcin Zajączkowski, k3v1n0x90


## 1.3 (2019-03-05)

No functional changes


## 1.3-RC1 (2019-01-22)

The theme for this release is to increase the information that is provided when an assertion failed.


### Potential breaking changes

#### code argument constraints are treated as implicit assertions

Before this release the code argument constrains worked by returning a boolean result.
This was fine if you just wanted to do a simple comparison, but it breaks down if you
need to do 5 comparisons. Users also often assumed that it worked like the assertions in
`then` blocks and didn’t add `&&` to chain multiple assertions together, so their constraint
ignored all before the next line.


```groovy
1 * mock.foo( { it.size() > 1
                it[0].length == 2 })
```


This would only use the length comparison, to make it work you had to add `&&`.
Another problem arises by having more than one comparison inside the constraints,
you don’t know which of the 5 comparisons failed. If you just expected one method
call you could use an explicit `assert` as a workaround, but since it immediately
breaks, you can’t use it if you want to have multiple different calls to the same
mock.


With 1.3 the above code will actually work as intended, and even more important it
will give actual feedback what didn’t match.


So what can break?


If you used the code argument constraint as a way of capturing
the argument value, then this will most likely not work anymore, since assignments
to already declared variables are forbidden in implicit assertion block.
If you still need access to the argument, you can use the response generator closure
instead.


```groovy
def extern = null

1 * mock.foo( { extern = it; it.size() > 0 })  // old
1 * mock.foo( { it.size() > 0 }) >> { extern = it[0] } // new
```


The added benefit of this changes is, that it clearly differentiates the condition from
the capture.


Another consequence of the change is, that the empty `{}` assertion block will now pass
instead of fail, since no assertion error is being treated as passing, while it required
a `true` result beforehand.


It is advised, that if you have multiple conditions joined by `&&`, that you remove
it to get individual assertions reports instead of a large joined block.


#### assertions with explicit messages now include power assertions output.

**Given**

```groovy
def a = 1
def b = 2
assert a == b : "Additional message"
```


**Before**

```
a == b

Additional message
```


**Now**

```
a == b
| |  |
1 |  2
  false

Additional message
```


If you relied on this behavior to hide some output, or to prevent a stack overflow due to a self referencing
data structure, then you need to move the condition into a separate method that just returns the boolean result.


### What’s New In This release

- Add implicit assertions for CodeArgument constraints [#956](https://github.com/spockframework/spock/pull/956)
- Add power assertion output to asserts with explicit message [#928](https://github.com/spockframework/spock/pull/928)
- Add support for mixed named and positional arguments in mocks [#919](https://github.com/spockframework/spock/pull/919)
- Add NamedParam support for groovy-2.5 with backport to 2.4 [#921](https://github.com/spockframework/spock/pull/921)
- Add special rendering for Set comparisons [#925](https://github.com/spockframework/spock/pull/925)
- Add identity hash code to type hints in comparison failures if they are identical
- Fix erroneous regex where an optional colon was defined instead of a non-capturing group [#931](https://github.com/spockframework/spock/pull/931)
- Improve CodeArgumentConstraint by supporting assertions [#918](https://github.com/spockframework/spock/pull/918)
- Improve IDE type inference in MockingApi [#920](https://github.com/spockframework/spock/pull/920)
- Improve reporting of TooFewInvocationsError [#912](https://github.com/spockframework/spock/pull/912)
- Improve render class loader for classes in comparison failures [#932](https://github.com/spockframework/spock/pull/932)
- Improve record class literal values to display FQCN in comparison failures [#935](https://github.com/spockframework/spock/pull/935)
- Improve filter Java 9+ reflection stack frames
- Improve show stacktrace of throwables in comparison failure result
- Improve use canonical class name in comparison failure results if present
- Improve render otherwise irrelevant expressions if they get a type hint in comparison failure [#936](https://github.com/spockframework/spock/pull/936)
- Fix do not convert implicit "this" expression like when calling the constructor of a non-static inner class [#930](https://github.com/spockframework/spock/pull/930)
- Fix class expression recording when there are comments with dots in the same line [#937](https://github.com/spockframework/spock/pull/937)


Thanks to all the contributors to this release: Björn Kautler, Marc Philipp, Marcin Zajączkowski, Martin Vseticka, Michael Kutz, Kacper Bublik


## 1.2 (2018-09-23)

Breaking Changes: Spock 1.2 drops support for Java 6, Groovy 2.0 and Groovy 2.3


### What’s New In This release

- Add support for Java 11+ ([#895](https://github.com/spockframework/spock/issues/895), [#902](https://github.com/spockframework/spock/issues/902), [#903](https://github.com/spockframework/spock/issues/903))
- Add Groovy 2.5.0 Variant for better Java 10+ Support
- Add `@SpringBean` and `@SpringSpy` inspired by `@MockBean`, Also add `@StubBeans` ([Docs](module_spring.md#_annotation_driven))
- Add `@UnwrapAopProxy` to make automatically unwrap SpringAopProxies
- Add `@AutoAttach` extension  ([Docs](extensions.md#_autoattach))
- Add `@Retry` extension ([Docs](extensions.md#_retry))
- Add flag to UnrollNameProvider to assert unroll expressions (set the system property `spock.assertUnrollExpressions` to `true`) ([#767](https://github.com/spockframework/spock/issues/767))
- Add automatic module name descriptors for Java 9
- Add configurable `condition` to `@Retry` extension to allow for customizing when retries should be attempted ([Docs](extensions.md#_retry))
- Improve `@PendingFeature` to now have an optional `reason` attribute ([#907](https://github.com/spockframework/spock/issues/907))
- Improve `@Retry` to be declarable on a spec class which will apply it to all feature methods in that class and subclasses ([Docs](extensions.md#_retry))
- Improve StepwiseExtension mark only subsequent features as skipped in case of failure ([#893](https://github.com/spockframework/spock/issues/893))
- Improve in assertions Spock now uses `DefaultGroovyMethods.dump` instead of `toString` if a class doesn’t override the default `Object.toString`.
- Improve `verifyAll` can now also have a target same as `with`
- Improve static type hints for `verifyAll` and `with`
- Improve reporting of exceptions during cleanup, they are now properly reported as suppressed exceptions instead of hiding the real exception
- Improve default responses for stubs, Java 8 types like `Optional` and `Streams` now return empty, `CompletableFuture` completes with `null` result
- Improve support for builder pattern, stubs now return themselves if the return type matches the type of the stub
- Improve tapestry support with by supporting `@ImportModule`
- Improve `constructorArgs` for spies can now accept a map directly without the need to wrap it in a list
- Improve [Guice Module](modules.md#_guice_module) now automatically attaches detached mocks
- Improve unmatched mock messages by using `dump` instead of `inspect` for classes which don’t provide a custom `toString`
- Improve spying on concrete instances to enable partial mocking
- Fix use String renderer for Class instances ([#909](https://github.com/spockframework/spock/issues/909))
- Fix mark new Spring extensions as @Beta ([#890](https://github.com/spockframework/spock/issues/890))
- Fix exclude groovy-groovysh from compile dependencies ([#882](https://github.com/spockframework/spock/issues/882))
- Fix `Retry.Mode.FEATURE` and `Retry.Mode.SETUP_FEATURE_CLEANUP` to make a test pass if a retry was successful.
- Fix issue with `@SpringBean` mocks throwing `InvocationTargetException` instead of actual declared exceptions ([#878](https://github.com/spockframework/spock/issues/878), [#887](https://github.com/spockframework/spock/issues/887))
- Fix void methods with implicit targets failing in `with` and `verifyAll` ([#886](https://github.com/spockframework/spock/issues/886))
- Fix SpockAssertionErrors and its subclasses now are properly `Serializable`
- Fix Spring injection of JUnit Rules, due to the changes in 1.1 the rules where initialized before Spring could inject them,
this has been fixed by performing the injection earlier in the process
- Fix SpringMockTestExecutionListener initializes lazy beans
- Fix OSGi Import-Package header
- Fix re-declare recorder variables ([#783](https://github.com/spockframework/spock/issues/783)), this caused annotations such as `@Slf4j` to break Specifications
- Fix MissingFieldException in DiffedObjectAsBeanRenderer
- Fix problems with nested `with` and `verifyAll` method calls
- Fix assertion of mock invocation order with nested invocations ([#475](https://github.com/spockframework/spock/issues/475))
- Fix ignore inferred type for Spies on existing instance
- General dependency update


Thanks to all the contributors to this release: Marc Philipp, Rob Elliot, jochenberger, Jan Papenbrock, Paul King, Marcin Zajączkowski, mrb-twx,
Alexander Kazakov, Serban Iordache, Xavier Fournet, timothy-long, John Osberg, AlexElin, Benjamin Muschko, Andreas Neumann, geoand,
Burk Hufnagel, signalw, Martin Vseticka, Tilman Ginzel


## 1.2-RC3 (2018-09-16)

### What’s New In This release

- Add support for Java 11+ ([#895](https://github.com/spockframework/spock/pull/895), [#902](https://github.com/spockframework/spock/pull/902), [#903](https://github.com/spockframework/spock/pull/903))
- Improve `@PendingFeature` to now have an optional `reason` attribute [#907](https://github.com/spockframework/spock/pull/907)
- Fix use String renderer for Class instances [#909](https://github.com/spockframework/spock/pull/909)
- Fix mark new Spring extensions as @Beta [#890](https://github.com/spockframework/spock/pull/890)
- Fix exclude groovy-groovysh from compile dependencies [#882](https://github.com/spockframework/spock/pull/882)


Thanks to all the contributors to this release: Marc Philipp, Marcin Zajączkowski, signalw


## 1.2-RC2 (2018-09-04)

### What’s New In This release

- Add configurable `condition` to `@Retry` extension to allow for customizing when retries should be attempted ([Docs](extensions.md#_retry))
- Fix `Retry.Mode.FEATURE` and `Retry.Mode.SETUP_FEATURE_CLEANUP` to make a test pass if a retry was successful.
- Improve `@Retry` to be declarable on a spec class which will apply it to all feature methods in that class and subclasses ([Docs](extensions.md#_retry))
- Improve StepwiseExtension mark only subsequent features as skipped in case of failure [#893](https://github.com/spockframework/spock/pull/893)
- Fix issue with `@SpringBean` mocks throwing `InvocationTargetException` instead of actual declared exceptions ([#878](https://github.com/spockframework/spock/pull/878), [#887](https://github.com/spockframework/spock/pull/887))
- Fix void methods with implicit targets failing in `with` and `verifyAll` [#886](https://github.com/spockframework/spock/pull/886)


Thanks to all the contributors to this release: Marc Philipp, Tilman Ginzel, Marcin Zajączkowski, Martin Vseticka


## 1.2-RC1 (2018-08-14)

Breaking Changes: Spock 1.2 drops support for Java 6, Groovy 2.0 and Groovy 2.3


### What’s New In This release

- Add Groovy 2.5.0 Variant for better Java 10 Support
- Add @SpringBean and @SpringSpy inspired by @MockBean, Also add @StubBeans
- Add @UnwrapAopProxy to make automatically unwrap SpringAopProxies
- Add flag to UnrollNameProvider to assert unroll expressions (set the system property `spock.assertUnrollExpressions` to `true`) [#767](https://github.com/spockframework/spock/issues/767)
- Add automatic module name descriptors for Java 9
- Add `@AutoAttach` extension ([Docs](extensions.md#_autoattach))
- Add `@Retry` extension ([Docs](extensions.md#_retry))
- Fix SpockAssertionErrors and its subclasses now are properly `Serializable`
- Fix Spring injection of JUnit Rules, due to the changes in 1.1 the rules where initialized before Spring could inject them,
this has been fixed by performing the injection earlier in the process
- Fix SpringMockTestExecutionListener initializes lazy beans
- Fix OSGi Import-Package header
- Fix re-declare recorder variables [#783](https://github.com/spockframework/spock/pull/783), this caused annotations such as `@Slf4j` to break Specifications
- Fix MissingFieldException in DiffedObjectAsBeanRenderer
- Fix problems with nested `with` and `verifyAll` method calls
- Fix assertion of mock invocation order with nested invocations [#475](https://github.com/spockframework/spock/pull/475)
- Fix ignore inferred type for Spies on existing instance
- Improve in assertions Spock now uses `DefaultGroovyMethods.dump` instead of `toString` if a class doesn’t override the default `Object.toString`.
- Improve `verifyAll` can now also have a target same as `with`
- Improve static type hints for `verifyAll` and `with`
- Improve reporting of exceptions during cleanup, they are now properly reported as suppressed exceptions instead of hiding the real exception
- Improve default responses for stubs, Java 8 types like `Optional` and `Streams` now return empty, `CompletableFuture` completes with `null` result
- Improve support for builder pattern, stubs now return themselves if the return type matches the type of the stub
- Improve tapestry support with by supporting `@ImportModule`
- Improve `constructorArgs` for spies can now accept a map directly without the need to wrap it in a list
- Improve [Guice Module](modules.md#_guice_module) now automatically attaches detached mocks
- Improve unmatched mock messages by using `dump` instead of `inspect` for classes which don’t provide a custom `toString`
- Improve spying on concrete instances to enable partial mocking
- General dependency update


Thanks to all the contributors to this release: Rob Elliot, jochenberger, Jan Papenbrock, Paul King, Marcin Zajączkowski, mrb-twx,
Alexander Kazakov, Serban Iordache, Xavier Fournet, timothy-long, John Osberg, AlexElin, Benjamin Muschko, Andreas Neumann, geoand,
Burk Hufnagel


### Known Issues

- Groovy 2.4.10 introduced a bug that interfered with the way `verifyAll` works, it has been fixed in 2.4.12


## 1.1 (2017-05-01)

### What’s New In This release

- Update docs to include info/examples for Spying instantiated objects
- Fix integer overflow that could occur when the OutOfMemoryError protection while comparing huge strings kicked in
- Improve rendering for OutOfMemoryError protection


## 1.1-rc-4 (2017-03-28)

This should be the last rc for 1.1


### What’s New In This release

- 15 merged pull requests
- Spies can now be created with an already existing target
- Fix for scoped Spring Beans
- Fix incompatibility with Spring 2/3 that was introduced in 1.1-rc-1
- Fix groovy compatibility
- Fix ByteBuddy compatibility
- Fix OutOfMemoryError when comparing huge strings
- Improve default response for `java.util.Optional<T>`, will now return empty optional
- Improve detection of Spring Boot tests
- Improve documentation for global extensions


Thanks to all the contributors to this release: Taylor Wicksell, Rafael Winterhalter, Marcin Zajączkowski, Eduardo Grajeda, Paul King, Andrii, Björn Kautler, Libor Rysavy


Known issues with groovy 2.4.10 which breaks a smoke test, but should have little impact on normal use [#709](https://github.com/spockframework/spock/pull/709).


## 1.1-rc-3 (released 2016-10-17)

Adds compatibility with ByteBuddy as an alternative to cglib for generating mocks and stubs for classes.


## 1.1-rc-2 (released 2016-08-22)

1.1 should be here soon but in the meantime there’s a new release candidate.


### What’s New In This release

- Support for the new test annotations in Spring Boot 1.4.
- Fixed the integration of JUnit method rules which now correctly happen "outside" the `setup` / `cleanup` methods.


Thanks to all the contributors to this release: Jochen Berger, Leonard Brünings, Mariusz Gilewicz, Tomasz Juchniewicz, Gamal Mateo, Tobias Schulte, Florian Wilhelm, Kevin Wittek


## 1.1-rc-1 (released 2016-06-30)

A number of excellent pull requests have been integrated into the 1.1 stream.
Currently some features are incubating.
We encourage users to try out these new features and provide feedback so we can finalize the content for a 1.1 release.


### What’s New In This release

- 44 merged pull requests
- The `verifyAll` method can be used to assert multiple boolean expressions *without* short-circuiting those after a failure.
For example:


```
then:
verifyAll {
  a == b
  b == c
}
```


- Detached mocks via the `DetachedMockFactory` and `SpockMockFactoryBean` classes see the [Spring Module Docs](module_spring.md#spring-module).
- Cells in a data table can refer to the current value for a column to the left.
- `Spy` can be used to create partial mocks for Java 8 interfaces with `default` methods just as it can for abstract classes.
- Improved power assert output when an exception occurs evaluating an assertion.
- A new `@PendingFeature` annotation to distinguish incomplete functionality from features with `@Ignore`.


Special thanks to all the contributors to this release: Dmitry Andreychuk, Aseem Bansal, Daniel Bechler, Fedor Bobin, Leonard Brünings, Leonard Daume, Marcin Erdmann, Jarl Friis, Søren Berg Glasius, Serban Iordache, Michal Kordas, Pap Lőrinc, Vlad Muresan, Etienne Neveu, Glyn Normington, David Norton, Magnus Palmér, Gus Power, Oliver Reissig, Kevin Wittek and Marcin Zajączkowski


## 1.0 (released 2015-03-02)

1.0 has arrived! Finally (and some years late) the version number communicates what
[Spock users](https://code.google.com/p/spock/wiki/WhoIsUsingSpock) have known for ages - that Spock isn’t only useful
and fun, but also reliable, mature, and here to stay. So please, go out and tell everyone who hasn’t been assimilated
that now is the time to join the party!


A special thanks goes to all our tireless speakers and supporters, only a few of which are listed here: Andres Almiray,
Cédric Champeau, David Dawson, Rob Fletcher, Sean Gilligan, Ken Kousen, Guillaume Laforge,
[NFJS Tour](https://www.nofluffjuststuff.com/home/main), Graeme Rocher, Baruch Sadogursky, Odin Hole Standal,
Howard M. Lewis Ship, Ken Sipe, Venkat Subramaniam, Russel Winder.


### What’s New In This Release

- [17 contributors](#_contributors), [21 resolved issues](#_resolved_issues), [18 merged pull requests](#_merged_pull_requests),
[some ongoing work](#_ongoing_work). No ground-breaking new features, but significant improvements and fixes across the board.
- Minimum runtime requirements raised to JRE 1.6 and Groovy 2.0.
- Improved and restyled reference documentation at https://docs.spockframework.org. Generated with
[Asciidoctor](https://asciidoctor.org/) (what else?).
- Maven plugin removed. Just let Maven Surefire run your Spock specs like your JUnit tests
(see [spock-example](https://github.com/spockframework/spock-example) project).
- Official support for Java 1.8, Groovy 2.3 and Groovy 2.4. Make sure to pick the `groovy-2.0` binaries for Groovy
2.0/2.1/2.2, `groovy-2.3` binaries for Groovy 2.3, and `groovy-2.4` binaries for Groovy 2.4 and higher.
- Improved infrastructure to allow for easier community involvement: Switch to
[GitHub issue tracker](https://issues.spockframework.org), [Windows](https://winbuilds.spockframework.org) and
[Linux](https://builds.spockframework.org) CI builds, pull requests automatically tested, all development on `master`
branch (bye-bye `groovy-x.y` branches!).


### Other News

- Follow our new [Twitter account](https://twitter.spockframework.org)
- Try these [new third-party extensions](#_new_third_party_extensions)
- Check out the upcoming [Java Testing with Spock](https://manning.com/kapelonis/) book from Manning


### What’s Up Next?

With a revamped build/release process and a reforming core team, we hope to release much more frequently from now on.
Another big focus will be to better involve the community and their valuable contributions. Last but not least, we are
finally shooting for a professional logo and website. Stay tuned for announcements!


Test Long And Prosper,


The Spock Team


---


### Contributors

17 awesome people contributed to this release:


- [Jordan Black](https://github.com/jblack10101)
- [Fedor Bobin](https://github.com/Fuud)
- [Leonard Brünings](https://github.com/leonard84)
- [cetnar](https://github.com/cetnar)
- [Luke Daley](https://github.com/ldaley)
- [David Dawson](https://github.com/daviddawson)
- [Scott G](https://github.com/selenium34)
- [Sean Gilligan](https://github.com/msgilligan)
- [Taha Hafeez](https://github.com/tawus)
- [Lari Hotari](https://github.com/lhotari)
- [Nicklas Lindgren](https://github.com/niligulmohar)
- [David W Millar](https://github.com/david-w-millar)
- [Peter Niederwieser](https://github.com/pniederw)
- [Jean-Baptiste Nallet](https://github.com/palmplam)
- [Opalo](https://github.com/Opalo)
- [Magda Stożek](https://github.com/magdzikk)
- [Ramazan VARLIKLI](https://github.com/rvarlikli)


### Resolved Issues

21 burning issues were fixed:


- [Create a example which uses ConfineMetaClassChanges](https://code.google.com/p/spock/issues/detail?id=221)
- [Mistakes in PollingConditions sphinx docs](https://code.google.com/p/spock/issues/detail?id=273)
- [Closure used as data value in where-block can’t be called with method syntax](https://code.google.com/p/spock/issues/detail?id=274)
- [old() expression blows up when part of failing condition](https://code.google.com/p/spock/issues/detail?id=276)
- [Reflect subsequent filtering/sorting in a spec’s JUnit description](https://code.google.com/p/spock/issues/detail?id=278)
- [After/AfterClass/Before/BeforeClass methods from superclass should not be called if they have been overrided in the derived class](https://code.google.com/p/spock/issues/detail?id=282)
- [Data values in where-block are not resolved in nested closures](https://code.google.com/p/spock/issues/detail?id=286)
- [spock-maven:0.7-groovy-2.0 has an invalid descriptor (and a workaround for this)](https://code.google.com/p/spock/issues/detail?id=290)
- [PollingConditions doesn’t report failed assertion](https://code.google.com/p/spock/issues/detail?id=291)
- [Provide a Specification.with() overload that states the expected target type](https://code.google.com/p/spock/issues/detail?id=292)
- [Problem with array arguments to mock methods](https://code.google.com/p/spock/issues/detail?id=294)
- [spock-tapestry should support @javax.inject.Inject and @InjectService](https://code.google.com/p/spock/issues/detail?id=296)
- [Compilation error when using multi assignment](https://code.google.com/p/spock/issues/detail?id=297)
- [Groovy mocks should allow to mock final classes/methods](https://code.google.com/p/spock/issues/detail?id=302)
- [Better generics support for mocks and stubs](https://code.google.com/p/spock/issues/detail?id=307)
- [GC calls to finalize() on mocks cause strict interaction specifications (0 * _) to fail intermittently](https://code.google.com/p/spock/issues/detail?id=338)
- [Multiple Assignment in when: and anything in cleanup:](https://code.google.com/p/spock/issues/detail?id=371)
- [Move OptimizeRunOrderSuite from spock-core to spock-maven to solve a problem with Android’s test runner](https://code.google.com/p/spock/issues/detail?id=385)
- [Support running on JDK 8](https://code.google.com/p/spock/issues/detail?id=391)
- [Release binary variants for Groovy 2.3 and Groovy 2.4](https://code.google.com/p/spock/issues/detail?id=392)
- [Port reference documentation to Asciidoc](https://code.google.com/p/spock/issues/detail?id=393)


### Merged Pull Requests

18 hand-crafted pull requests were merged or cherry-picked:


- [Update extensions.rst](https://github.com/spockframework/spock/pull/51)
- [allow one column data-table to be passed as parameter](https://github.com/spockframework/spock/pull/48)
- [Use https:// link to Maven Central](https://github.com/spockframework/spock/pull/45)
- [Change Snapshot Repository to use https:// URL](https://github.com/spockframework/spock/pull/44)
- [Fix incorrect code listing in docs](https://github.com/spockframework/spock/pull/43)
- [Minor documentation corrections: spelling, code examples. README.md corr…](https://github.com/spockframework/spock/pull/41)
- [added manifest to core.gradle to allow spock core to work in OSGi land](https://github.com/spockframework/spock/pull/40)
- [Allow Build on Windows](https://github.com/spockframework/spock/pull/38)
- [Small typo fixed](https://github.com/spockframework/spock/pull/33)
- [Update interaction_based_testing.rst](https://github.com/spockframework/spock/pull/32)
- [Closure used as data value in where-block can’t be called with method syntax](https://github.com/spockframework/spock/pull/31)
- [Added docs for Stepwise, Timeout, Use, ConfineMetaClassChanges, AutoClea…](https://github.com/spockframework/spock/pull/30)
- [Spring @ContextHierarchy support](https://github.com/spockframework/spock/pull/16)
- [Add groovy console support for the specs project, to ease debugging of the AST.](https://github.com/spockframework/spock/pull/14)
- [Update spock-report/src/test/groovy/org/spockframework/report/sample/Fig…](https://github.com/spockframework/spock/pull/13)
- [spock-tapestry: added support for @InjectService, @javax.inject.Inject](https://github.com/spockframework/spock/pull/12)
- [missing code](https://github.com/spockframework/spock/pull/11)
- [Support overriding Junit After*/Before* methods in the derived class](https://github.com/spockframework/spock/pull/10)(


### New Third Party Extensions

These awesome extensions have been published or updated:


- [Spock Subjects-Collaborators Extension](https://github.com/marcingrzejszczak/spock-subjects-collaborators-extension)
- [Spock Reports Extension](https://github.com/renatoathaydes/spock-reports)


### Ongoing Work

These great features didn’t make it into this release (but hopefully the next!):


- [Spock reports](https://spockframework.github.io/spock/sampleReports/Ninja%20Commander.html)
- [Render exceptions in conditions as condition failure](https://github.com/spockframework/spock/pull/49)
- [Soft asserts: check all then throw all failures](https://github.com/spockframework/spock/pull/50)
- [Detached mocks](https://github.com/spockframework/spock/pull/17)


## 0.7 (released 2012-10-08)

### Snapshot Repository Moved

Spock snapshots are now available from https://oss.sonatype.org/content/repositories/snapshots/org/spockframework/.


### New Reference Documentation

The new Spock reference documentation is available at https://docs.spockframework.org.
It will gradually replace the documentation at https://wiki.spockframework.org.
Each Spock version is documented separately (e.g. https://docs.spockframework.org/en/spock-0.7-groovy-1.8).
Documentation for the latest Spock snapshot is at https://docs.spockframework.org/en/latest.
As of Spock 0.7, the chapters on [Data Driven Testing](data_driven_testing.md#data-driven-testing) and
[Interaction Based Testing](interaction_based_testing.md#interaction-based-testing) are complete.


### Improved Mocking Failure Message for `TooManyInvocationsError`

The diagnostic message accompanying a `TooManyInvocationsError` has been greatly improved.
Here is an example:


```
Too many invocations for:

3 * person.sing(_)   (4 invocations)

Matching invocations (ordered by last occurrence):

2 * person.sing("do")   <-- this triggered the error
1 * person.sing("re")
1 * person.sing("mi")
```


[Reference Documentation](interaction_based_testing.md#_verification_of_interactions)


### Improved Mocking Failure Message for `TooFewInvocationsError`

The diagnostic message accompanying a `TooFewInvocationsError` has been greatly improved.
Here is an example:


```
Too few invocations for:

1 * person.sing("fa")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * person.sing("re")
1 * person.say("fa")
1 * person2.shout("mi")
```


[Reference Documentation](interaction_based_testing.md#_verification_of_interactions)


### Stubs

Besides mocks, Spock now has explicit support for stubs:


```groovy
def person = Stub(Person)
```


A stub is a restricted form of mock object that responds to invocations without ever demanding them.
Other than not having a cardinality, a stub’s interactions look just like a mock’s interactions.
Using a stub over a mock is an effective way to communicate its role to readers of the specification.


[Reference Documentation](interaction_based_testing.md#Stubs)


### Spies

Besides mocks, Spock now has support for spies:


```groovy
def person = Spy(Person, constructorArgs: ["Fred"])
```


A spy sits atop a real object, in this example an instance of class `Person`. All invocations on the spy
that don’t match an interaction are delegated to that object. This allows to listen in on and selectively
change the behavior of the real object. Furthermore, spies can be used as partial mocks.


[Reference Documentation](interaction_based_testing.md#Spies)


### Declaring Interactions at Mock Creation Time

Interactions can now be declared at mock creation time:


```groovy
def person = Mock(Person) {
    sing() >> "tra-la-la"
    3 * eat()
}
```


This feature is particularly attractive for [_stubs](#_stubs).


[Reference Documentation](interaction_based_testing.md#Stubs)


### Groovy Mocks

Spock now offers specialized mock objects for spec’ing Groovy code:


```groovy
def mock = GroovyMock(Person)
def stub = GroovyStub(Person)
def spy = GroovySpy(Person)
```


A Groovy mock automatically implements `groovy.lang.GroovyObject`. It allows stubbing and mocking
of dynamic methods just like for statically declared methods. When a Groovy mock is called from Java
rather than Groovy code, it behaves like a regular mock.


[Reference Documentation](interaction_based_testing.md#GroovyMocks)


### Global Mocks

A Groovy mock can be made *global*:


```groovy
GroovySpy(Person, global: true)
```


A global mock can only be created for a class type. It effectively replaces all instances of that type and makes them
amenable to stubbing and mocking. (You may know this behavior from Groovy’s `MockFor` and `StubFor` facilities.)
Furthermore, a global mock allows mocking of the type’s constructors and static methods.


[Reference Documentation](interaction_based_testing.md#MockingAllInstancesOfAType)


### Grouping Conditions with Same Target Object

Inspired from Groovy’s `Object.with` method, the `Specification.with` method allows to group conditions
involving the same target object:


```groovy
def person = new Person(name: "Fred", age: 33, sex: "male")

expect:
with(person) {
    name == "Fred"
    age == 33
    sex == "male"
}
```


### Grouping Interactions with Same Target Object

The `with` method can also be used for grouping interactions:


```groovy
def service = Mock(Service)
app.service = service

when:
app.run()

then:
with(service) {
    1 * start()
    1 * act()
    1 * stop()
}
```


[Reference Documentation](interaction_based_testing.md#_grouping_interactions_with_same_target)


### Polling Conditions

`spock.util.concurrent.PollingConditions` joins `AsyncConditions` and `BlockingVariable(s)` as another utility for
testing asynchronous code:


```groovy
def person = new Person(name: "Fred", age: 22)
def conditions = new PollingConditions(timeout: 10)

when:
Thread.start {
    sleep(1000)
    person.age = 42
    sleep(5000)
    person.name = "Barney"
}

then:
conditions.within(2) {
    assert person.age == 42
}

conditions.eventually {
    assert person.name == "Barney"
}
```


### Experimental DSL Support for Eclipse

Spock now ships with a DSL descriptor that lets Groovy Eclipse better
understand certain parts of Spock’s DSL. The descriptor is automatically
detected and activated by the IDE. Here is an example:


```groovy
// currently need to type variable for the following to work
Person person = new Person(name: "Fred", age: 42)

expect:
with(person) {
    name == "Fred" // editor understands and auto-completes 'name'
    age == 42      // editor understands and auto-completes 'age'
}
```


Another example:


```groovy
def person = Stub(Person) {
    getName() >> "Fred" // editor understands and auto-completes 'getName()'
    getAge() >> 42      // editor understands and auto-completes 'getAge()'
}
```


DSL support is activated for Groovy Eclipse 2.7.1 and higher. If necessary,
it can be deactivated in the Groovy Eclipse preferences.


### Experimental DSL Support for IntelliJ IDEA

Spock now ships with a DSL descriptor that lets Intellij IDEA better
understand certain parts of Spock’s DSL. The descriptor is automatically
detected and activated by the IDE. Here is an example:


```groovy
def person = new Person(name: "Fred", age: 42)

expect:
with(person) {
    name == "Fred" // editor understands and auto-completes 'name'
    age == 42      // editor understands and auto-completes 'age'
}
```


Another example:


```groovy
def person = Stub(Person) {
    getName() >> "Fred" // editor understands and auto-completes 'getName()'
    getAge() >> 42      // editor understands and auto-completes 'getAge()'
}
```


DSL support is activated for IntelliJ IDEA 11.1 and higher.


### Splitting up Class Specification

Parts of class `spock.lang.Specification` were pulled up into two new super classes: `spock.lang.MockingApi`
now contains all mocking-related methods, and `org.spockframework.lang.SpecInternals` contains internal methods
which aren’t meant to be used directly.


### Improved Failure Messages for `notThrown` and `noExceptionThrown`

Instead of just passing through exceptions, `Specification.notThrown` and `Specification.noExceptionThrown`
now fail with messages like:


```
Expected no exception to be thrown, but got 'java.io.FileNotFoundException'

Caused by: java.io.FileNotFoundException: ...
```


### `HamcrestSupport.expect`

Class `spock.util.matcher.HamcrestSupport` has a new `expect` method that makes
[Hamcrest](https://hamcrest.org/JavaHamcrest/) assertions read better in then-blocks:


```groovy
when:
def x = computeValue()

then:
expect x, closeTo(42, 0.01)
```


### @Beta

Recently introduced classes and methods may be annotated with `@Beta`, as a sign that they may still undergo incompatible
changes. This gives us a chance to incorporate valuable feedback from our users. (Yes, we need your feedback!) Typically,
a `@Beta` annotation is removed within one or two releases.


### Fixed Issues

See the [issue tracker](https://code.google.com/p/spock/issues/list?can=1&q=label%3AMilestone-0.7) for a list of fixed issues.


## 0.6 (released 2012-05-02)

### Mocking Improvements

The mocking framework now provides better diagnostic messages in some cases.


Multiple result declarations can be chained. The following causes method bar to throw an `IOException` when first called,
return the numbers one, two, and three on the next calls, and throw a `RuntimeException` for all subsequent calls:


```groovy
foo.bar() >> { throw new IOException() } >>> [1, 2, 3] >> { throw new RuntimeException() }
```


It’s now possible to match any argument list (including the empty list) with `foo.bar(*_)`.


Method arguments can now be constrained with [Hamcrest](https://hamcrest.org/JavaHamcrest/) matchers:


```groovy
import static spock.util.matcher.HamcrestMatchers.closeTo

...

1 * foo.bar(closeTo(42, 0.001))
```


### Extended JUnit Rules Support

In addition to rules implementing `org.junit.rules.MethodRule` (which has been deprecated in JUnit 4.9), Spock now also
supports rules implementing the new `org.junit.rules.TestRule` interface. Also supported is the new `@ClassRule`
annotation. Rule declarations are now verified and can leave off the initialization part. I that case Spock will
automatically initialize the rule by calling the default constructor. The `@TestName` rule, and rules in general, now
honor the `@Unroll` annotation and any defined naming pattern.


See [Issue 240](https://code.google.com/p/spock/issues/detail?id=240) for a known limitation with Spock’s TestRule support.


### Condition Rendering Improvements

When two objects are compared with the `==` operator, they are unequal, but their string representations are the same,
Spock will now print the objects' types:


```
enteredNumber == 42
|             |
|             false
42 (java.lang.String)
```


### JUnit Fixture Annotations

Fixture methods can now be declared with JUnit’s `@Before`, `@After`, `@BeforeClass`, and `@AfterClass` annotations,
as an addition or alternative to Spock’s own fixture methods. This was particularly needed for Grails 2.0 support.


### Tapestry 5.3 Support

Thanks to a contribution from [Howard Lewis Ship](https://howardlewisship.com/), the Tapestry module is now compatible
with Tapestry 5.3. Older 5.x versions are still supported.


### IBM JDK Support

Spock now runs fine on IBM JDKs, working around a bug in the IBM JDK’s verifier.


### Improved JUnit Compatibility

`org.junit.internal.AssumptionViolatedException` is now recognized and handled as known from JUnit. `@Unrolled` methods
no longer cause "yellow" nodes in IDEs.


### Improved `@Unroll`

The `@Unroll` naming pattern can now be provided in the method name, instead of as an argument to the annotation:


```groovy
@Unroll
def "maximum of #a and #b is #c"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
}
```


The naming pattern now supports property access and zero-arg method calls:


```groovy
@Unroll
def "#person.name.toUpperCase() is #person.age years old"() { ... }
```


The `@Unroll` annotation can now be applied to a spec class. In this case, all data-driven feature methods in the class
will be unrolled.


### Improved `@Timeout`

The `@Timeout` annotation can now be applied to a spec class. In this case, the timeout applies to all feature methods
(individually) that aren’t already annotated with `@Timeout`. Timed methods are now executed on the regular test
framework thread. This can be important for tests that rely on thread-local state (like Grails integration tests).
Also the interruption behavior has been improved, to increase the chance that a timeout can be enforced.


The failure exception that is thrown when a timeout occurs now contains the stacktrace of test execution, allowing you
to see where the test was “stuck” or how far it got in the allocated time.


### Improved Data Table Syntax

Table cells can now be separated with double pipes. This can be used to visually set apart expected outputs from
provided inputs:


```groovy
...
where:
a | b || sum
1 | 2 || 3
3 | 1 || 4
```


### Groovy 1.8/2.0 Support

Spock 0.6 ships in three variants for Groovy 1.7, 1.8, and 2.0. Make sure to pick the right version - for example,
for Groovy 1.8 you need to use spock-core-0.6-groovy-1.8 (likewise for all other modules). The Groovy 2.0 variant
is based on Groovy 2.0-beta-3-SNAPSHOT and only available from https://m2repo.spockframework.org. The Groovy 1.7 and
1.8 variants are also available from Maven Central. The next version of Spock will no longer support Groovy 1.7.


### Grails 2.0 Support

Spock’s Grails plugin was split off into a separate project and now lives at https://github.spockframework.org/spock-grails.
The plugin supports both Grails 1.3 and 2.0.


The Spock Grails plugin supports all of the new Grails 2.0 test mixins, effectively deprecating the existing unit
testing classes (e.g. UnitSpec). For integration testing, IntegrationSpec must still be used.


### IntelliJ IDEA Integration

The folks from [JetBrains](https://www.jetbrains.com) have added a few handy features around data tables. Data tables
will now be layed out automatically when reformatting code. Data variables are no longer shown as "unknown" and have
their types inferred from the values in the table (!).


### GitHub Repository

All source code has moved to https://github.spockframework.org/. The [Grails Spock plugin](https://github.spockframework.org/spock-grails),
[Spock Example](https://github.com/spockframework/spock-example) project, and
[Spock Web Console](https://github.spockframework.org/spockwebconsole) now have their own GitHub projects.
Also available are slides and code for various Spock presentations (such as
[this one](https://github.spockframework.org/smarter-testing-with-spock)).


### Gradle Build

Spock is now exclusively built with Gradle. Building Spock yourself is as easy as cloning the
[Github repo](https://github.spockframework.org/spock) and executing `gradlew build`. No build tool installation is
required; the only prerequisite for building Spock is a JDK installation (1.5 or higher).


### Fixed Issues

See the [issue tracker](https://code.google.com/p/spock/issues/list?can=1&q=label%3AMilestone-0.6) for a list of fixed issues.

