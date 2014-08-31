[![Linux Build Status](https://drone.io/github.com/spockframework/spock/status.png)](https://drone.io/github.com/spockframework/spock/latest)
[![Windows Build Status](https://ci.appveyor.com/api/projects/status/o4hljlr3q44m8vf1/branch/groovy-1.9)](https://ci.appveyor.com/project/pniederw/spock/branch/groovy-1.9)

Spock Framework
===============

Spock is a developer testing and specification framework for Java and [Groovy](http://groovy.codehaus.org) applications.
To learn more about Spock, visit http://spockframework.org. To run a sample spec in your browser, go to
http://webconsole.spockframework.org.

Latest Versions
---------------
The latest release version is **0.7** (0.7-groovy-1.8, 0.7-groovy-2.0), released on 2012-10-08. The current development
version is **1.0-SNAPSHOT** (1.0-groovy-1.8-SNAPSHOT, 1.0-groovy-2.0-SNAPSHOT, 1.0-groovy-2.3-SNAPSHOT).

Releases are available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cspock).
Development snapshots are available from [Sonatype OSS](https://oss.sonatype.org/content/repositories/snapshots/org/spockframework/).

Modules
-------
* spock-core -- Core framework. This is the only mandatory module.
* spock-specs -- Specifications for spock-core, implemented using Spock. Not required for using Spock.
* spock-maven -- Extended [Maven](http://maven.apache.org/) support. Note that this module is optional;
it is *not* required for using Spock with Maven.
* spock-spring -- Integration with the [Spring TestContext Framework]
(http://static.springsource.org/spring/docs/3.2.1.RELEASE/spring-framework-reference/html/testing.html#testcontext-framework).
* spock-tapestry -- Integration with the [Tapestry 5](http://tapestry.apache.org/tapestry5/) IoC container.
* spock-guice -- Integration with [Guice](http://code.google.com/p/google-guice/) 2/3.
* spock-unitils -- Integration with [Unitils](http://www.unitils.org/).
* spock-report -- Interactive, business-friendly HTML reports.
* spock-grails -- The Grails plugin has become its own project hosted at https://github.spockframework.org/spock-grails.

Building Spock
--------------
The only prerequisite is that you have JDK 5 or higher installed.

After cloning the project, type `./gradlew clean build` (Windows: `gradlew clean build`). All build dependencies,
including [Gradle](http://www.gradle.org) itself, will be downloaded automatically (unless already present).

Resources
---------
* Spock Homepage -- http://spockframework.org
* Spock Web Console -- http://webconsole.spockframework.org
* GitHub Organization -- http://github.spockframework.org
* New Reference Documentation -- http://docs.spockframework.org/
* Old Wiki Documentation -- http://wiki.spockframework.org/SpockBasics
* Javadoc -- http://javadoc.spockframework.org/latest
* Groovydoc -- http://groovydoc.spockframework.org/latest
* User Forum -- http://forum.spockframework.org
* Developer Forum -- http://dev.forum.spockframework.org
* Issue Tracker -- http://issues.spockframework.org
* Build Server -- http://builds.spockframework.org
* Example Spock Project -- https://github.com/spockframework/spock-example

Contributions are always welcome! Please take a look at our [ways to contribute page](https://github.com/spockframework/spock/blob/groovy-1.8/CONTRIBUTING.md) for more information.

If you have any comments or questions, please direct them to the [user forum](http://forum.spockframework.org).
All feedback is appreciated!

Happy spec'ing!

Peter Niederwieser<br>
Creator, Spock Framework<br>
[Twitter](http://twitter.com/pniederw) | [Blog](http://blog.spockframework.org)
