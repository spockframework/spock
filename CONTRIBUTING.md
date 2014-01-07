## Ways to Contribute

There are many ways to contribute to Spock:

* Helping to build a bigger community, for example by submitting Spock-related talks or answering Spock questions on [Stack Overflow](http://stackoverflow.com/questions/tagged/spock).
* Helping to improve the [reference documentation](http://docs.spockframework.org) [(source)](https://github.com/spockframework/spock/tree/groovy-1.8/docs).
* Fixing open issues listed in the [issue tracker](http://issues.spockframework.org).
* Proposing, discussing, and implementing new features.
* Designing a Spock logo and/or website. (Disclaimer: Our expectations are high.)

All forms of contribution are very much appreciated.

## Communication

Good communication makes a big difference. That's why we would love to hear from you! Don't hesitate to get in touch via the [issue tracker](http://issues.spockframework.org), [user forum](http://forum.spockframework.org), or [dev forum](http://dev.forum.spockframework.org). Choose whatever medium feels most appropriate.

## Contributing Code/Docs

To contribute code or documentation, please submit a pull request to the [GitHub repository](http://github.spockframework.org/spock). The repository has one branch per major Groovy version. As of this writing, the active branches are `groovy-1.8` for Groovy 1.8.x, and `groovy-1.9` for Groovy 2.x (which was historically named 1.9). Unless stated otherwise, all pull requests should be made against the "lowest" active branch (`groovy-1.8` as of this writing). Accepted pull requests will be merged into "higher" branches (e.g. `groovy-1.9`) by the Spock team.

A good way to familiarize yourself with the codebase and contribution process is to look for and tackle low-hanging fruits in the [issue tracker](http://issues.spockframework.org). Before embarking on a more ambitious contribution, please quickly [get in touch](#communication) with us. This will help to make sure that the contribution is aligned with Spock's overall direction and goals, and gives us a chance to guide design and implementation where needed. **We appreciate your effort, and want to avoid a situation where a contribution requires extensive rework (by you or by us), sits in the queue for a long time, or cannot be accepted at all!**

### What Makes a Good Pull Request

When reviewing pull requests, we value the following qualities (in no particular order):

* Carefully crafted, clean and concise code
* Small, focused commits that tackle one thing at a time
* New code blends in well with existing code, respecting established coding standards and practices
* Tests are updated/added along with the code, communicate intent and cover important cases (see [Tests] below for more information)
* Documentation (Javadoc, Groovydoc, reference documentation) is updated/added along with the code

Don't be intimitated by these words. Pull requests that satisfy Spock's overall direction and goals (see above), are crafted carefully, and aren't aiming too high, have a good chance of getting accepted. Before doing so, we may ask for some concrete improvements to be made, in which case we hope for your cooperation.

### Implementation Language

The implementation language for the [https://github.spockframework.org/spock/tree/groovy-1.8/spock-core) module is Java. Java is also the default language for all other modules (except `spock-specs`), but it's fine to use Groovy when there is a concrete reason. As a general guideline, use the same language as the code around you.

### Compatibility

Spock supports JRE 1.5 and higher. Therefore, language features and APIs that are only available in Java 1.6 or higher cannot be used. (A typical pitfall is to use `String#isEmpty`, which only exists since Java 1.6.) Exceptions to this rule need to be discussed beforehand. The same goes for changes to user-visible behavior.

### Tests

All tests are written in Spock. Tests for `spock-core` are located in the `spock-specs` project; all other projects have co-located tests. For each user-visible behavior, a functional test is required. Functional tests for `spock-core` are located under [`spock-specs/src/test/groovy/org/spockframework/smoke`](https://github.spockframework.org/spock/tree/groovy-1.8/spock-specs/src/test/groovy/org/spockframework/smoke).

## Development Tools

### Command Line Build

Spock is built with [Gradle](http://www.gradle.org). The only prerequsite for executing the build is an installation of JDK 1.5 (or higher). After cloning the [GitHub repository](http://github.spockframework.org/spock), cd into the top directory and execute `gradlew build` (Windows: `.\gradlew build`). The build should succeed without any errors. `gradlew tasks` lists the available tasks. Always use the Gradle Wrapper (`gradlew` command) rather than your own Gradle installation.

### CI Build

Each push to an active branch of the official GitHub repository triggers a [drone.io build](http://builds.spockframework.org). The CI build uses JDK 1.6, and therefore won't catch usages of Java 1.6 only APIs. (Given that we strive for Java 1.5 compatibility, this is unfortunate, but drone.io doesn't offer JDK 1.5.) However, the build will catch usages of Java 1.7+ only APIs.

### IDE Setup

Using an IDE is recommended but not mandatory. Whether or not you use an IDE, please make sure that `gradlew build` succeeds before submitting a pull request.

#### IntelliJ IDEA

IntelliJ IDEA 13+ is the preferred IDE for developing Spock. To import the Gradle build into IDEA, choose "File->Import Project" and select the top-level `build.gradle` file. This should result in a fully functional IDE setup where:

* Git VCS integration is configured
* Important formatter settings are configured (e.g. two spaces indent) 
* Both Java and Groovy code compiles without problems
* All tests can be run without problems 

From time to time (e.g. when someone has added a new dependency), it may be necessary to resync the IDEA project with the Gradle build. This is done by pushing the "Refresh" button in the Gradle Tool Window.

#### Eclipse

Eclipse 3.7+ with latest [Groovy plugin](http://groovy.codehaus.org/Eclipse+Plugin) should work reasonably well for developing Spock. To import the Gradle build into Eclipse, either run `gradlew eclipse` and import the generated Eclipse projects via "File->Import->General->Existing Project into Workspace", or install the [Eclipse Gradle Tooling](https://github.com/spring-projects/eclipse-integration-gradle/) and import via "File->Import->Gradle->Gradle Project". Either method should result in an IDE setup where:

* Both Java and Groovy code compiles without problems
* All tests can be run without problems

From time to time (e.g. when someone has added a new dependency), it may be necessary to resync the Eclipse project with the Gradle build. Depending on the method of import (see above), this is done by re-running `gradlew [cleanEclipse] eclipse` and then refreshing all Eclipse projects (F5), or by selecting "Gradle->Refresh All" in the Package Explorer's context menu.

If you encounter any problems with the IDE setup, or want to make some improvements to it, please [get in touch](#communication) with us.

## The End

Thanks for reading this far. We are looking forward to your contributions!

The Spock Team

