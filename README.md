[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/spockframework/spock/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.spockframework/spock-core.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.spockframework%22%20AND%20a%3A%22spock-core%22)
[![Linux Build Status](https://img.shields.io/travis/spockframework/spock/master.svg?label=Linux%20Build)](https://travis-ci.org/spockframework/spock)
[![Windows Build Status](https://img.shields.io/appveyor/ci/spockframework/spock/master.svg?label=Windows%20Build)](https://ci.appveyor.com/project/spockframework/spock/branch/master)
[![Codecov](https://codecov.io/gh/spockframework/spock/branch/master/graph/badge.svg)](https://codecov.io/gh/spockframework/spock)
[![Gitter](https://badges.gitter.im/spockframework/spock.svg)](https://gitter.im/spockframework/spock?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Spock Framework
===============

Spock is a developer testing and specification framework for Java and [Groovy](http://groovy.codehaus.org) applications.
To learn more about Spock, visit http://spockframework.org. To run a sample spec in your browser, go to
http://webconsole.spockframework.org.

Latest Versions
---------------
The latest release version is **1.1-rc-4** (1.1-groovy-2.0-rc-4, 1.1-groovy-2.3-rc-4, 1.1-groovy-2.4-rc-4), released on 2016-10-17. The
current development version is **1.1-SNAPSHOT** (1.1-groovy-2.0-SNAPSHOT, 1.1-groovy-2.3-SNAPSHOT, 1.1-groovy-2.4-SNAPSHOT).

Releases are available from [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cspock).
Development snapshots are available from [Sonatype OSS](https://oss.sonatype.org/content/repositories/snapshots/org/spockframework/).

Modules
-------
* spock-core -- Core framework. This is the only mandatory module.
* spock-specs -- Specifications for spock-core, implemented using Spock. Not required for using Spock.
* spock-spring -- Integration with the [Spring TestContext Framework]
(http://docs.spring.io/spring/docs/4.1.5.RELEASE/spring-framework-reference/html/testing.html#testcontext-framework).
* spock-tapestry -- Integration with the [Tapestry 5](http://tapestry.apache.org/tapestry5/) IoC container.
* spock-guice -- Integration with [Guice](http://code.google.com/p/google-guice/) 2/3.
* spock-unitils -- Integration with [Unitils](http://www.unitils.org/).
* spock-report -- Interactive, business-friendly HTML reports.

Building
--------

### Supported versions
Spock is supported for Java version 6, 7, and 8.
Spock is supported for Groovy version 2.0 (Not supported for Java 8+), 2.3, 2.4

The tests are testing spock with a specific versions (variants) of groovy. Default is groovy version 2.4

The groovy 2.4 variant should pass on all supported JDK versions:

```
./gradlew clean build
```

(Windows: `gradlew clean build`).
All build dependencies, including
the [build tool](http://www.gradle.org) itself, will be downloaded
automatically (unless already present).

If you are using JDK 6 or 7 you can specify groovy version 2.0 with
using `-Dvariant=2.0` and it should pass as well:

```
./gradlew clean build -Dvariant=2.0
```

Contributing
------------
Contributions are welcome! Please see the [contributing page](https://github.com/spockframework/spock/blob/master/CONTRIBUTING.md) for detailed instructions.

Support
-------
If you have any comments or questions, please direct them to the [user forum](http://forum.spockframework.org).
All feedback is appreciated!

Links
-----
* Spock Homepage -- http://spockframework.org
* Spock Web Console -- http://webconsole.spockframework.org
* GitHub Organization -- http://github.spockframework.org
* Reference Documentation -- http://docs.spockframework.org
* Old Wiki -- http://wiki.spockframework.org
* Javadoc -- http://javadoc.spockframework.org
* User Forum -- http://forum.spockframework.org
* Developer Forum -- http://dev-forum.spockframework.org
* Issue Tracker -- http://issues.spockframework.org
* Build Server -- http://builds.spockframework.org
* Spock Example Project -- http://github.spockframework.org/spock-example
* Twitter -- http://twitter.spockframework.org

Live Long And Prosper!

The Spock Framework Team
