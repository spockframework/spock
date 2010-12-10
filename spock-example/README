Spock Framework Example Project
===============================

The purpose of this project is to help you get started with Spock. The project includes several example specifications and build scripts for Ant, Gradle, and Maven. It also makes it easy to create an Eclipse or IDEA project, allowing you to run the example specs from within your IDE.
All three builds (Ant, Gradle, Maven) will automatically download all required dependencies, compile the project, and finally run the example specs. The Gradle build goes one step further by bootstrapping itself, alleviating the need to have a build tool preinstalled.

Prerequisites
-------------
- JDK 5 or higher
- Ant 1.7 or higher (for Ant build)
- Maven 2.x (for Maven build)

Building with Ant
-----------------
Type: ant clean test

Downloaded files will be stored in the local Maven repository (typically "<user_home>/.m2/repository").

Building with Gradle
--------------------
Type: ./gradlew clean test

Downloaded files (including the Gradle distribution itself) will be stored in the Gradle user home directory (typically "<user_home>/.gradle").

Building with Maven
-------------------
Type: mvn clean test

Downloaded files will be stored in the local Maven repository (typically "<user_home>/.m2/repository").

Creating an Eclipse project
---------------------------
Type: ./gradlew cleanEclipse eclipse

Make sure you have a recent version of the Groovy Eclipse plugin installed. After importing the generated project into a workspace, go to Preferences->Java->Build Path->Classpath Variables and add a variable named GRADLE_CACHE with value "<user_home>/.gradle/cache". (If you have an environment variable GRADLE_USER_HOME set, the correct value is "<GRADLE_USER_HOME>/cache".) You should now be able to build the project, and to run the specs like you would run a JUnit test. See http://wiki.spockframework.org/GettingStarted#Eclipse for more information on how to get started with Spock and Eclipse.

Creating an IDEA project
---------------------------
Type: ./gradlew cleanIdea idea

Open the generated project in IDEA. You should now be able to build the project, and to run the specs like you would run a JUnit test.

Getting hold of the Jars used in this project
---------------------------------------------
Type: ./gradlew collectJars

The Jars will be copied to build/output/lib. The comments in build.gradle explain what they are needed for.

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

