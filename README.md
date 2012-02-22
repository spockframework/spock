Spock Framework README
======================

Spock is a developer testing and specification framework for Java and Groovy applications. To learn more about Spock,
visit http://spockframework.org. To run your first spec right away, visit http://meet.spockframework.org.

Current release versions: 0.5-groovy-1.6, 0.5-groovy-1.7, 0.5-groovy-1.8 (released 2010-12-10)
Current development versions: 0.6-groovy-1.7-SNAPSHOT, 0.6-groovy-1.8-SNAPSHOT

Modules
-------
spock-core: Core framework.

spock-specs: Specifications for spock-core, written with Spock. Not required for using the framework.

spock-maven: Extended Maven support (optional).

spock-example: Self-contained example project with Ant, Gradle, and Maven build. See spock-example/README for more information.

spock-spring: Integration with the Spring TestContext Framework.

spock-tapestry: Integration with the Tapestry 5 IoC container.

spock-guice: Integration with Guice 2.

spock-unitils: Integration with Unitils (http://www.unitils.org/).

Gradle Build
------------
Prerequisites: JDK 5 or higher

Type: ./gradlew clean build

If not already present, build dependencies (including Gradle itself) will be downloaded automatically.

Maven Build
-----------
Prerequisites: JDK5 or higher, Maven 2.x

Type: mvn clean install

If not already present, build dependencies will be downloaded automatically.

Further Resources
-----------------
Spock homepage        http://spockframework.org
Spock web console     http://meet.spockframework.org
Main documentation    http://wiki.spockframework.org/SpockBasics
User discussion group http://forum.spockframework.org
Dev discussion group  http://dev.forum.spockframework.org
Issue tracker         http://issues.spockframework.org
Build server          http://builds.spockframework.org
Maven repository      http://m2repo.spockframework.org (releases are also available from Maven Central)
Spock blog            http://blog.spockframework.org
Spock on Twitter      http://twitter.com/pniederw

Ant homepage    http://ant.apache.org
Gradle homepage http://www.gradle.org
Groovy homepage http://groovy.codehaus.org
Maven homepage  http://maven.apache.org

If you have any comments or questions, please direct them to the Spock discussion group. All feedback is appreciated!

Happy spec'ing!
Peter Niederwieser
Creator, Spock Framework