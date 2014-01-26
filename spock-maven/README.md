spock-maven
===========

Extended [Maven 2](http://maven.apache.org/) support by automatically adding all found Specification classes
to the [Maven Surefire Plugin](http://maven.apache.org/surefire/maven-surefire-plugin/) execution processes.

Notes:

- This module is optional; it is *not* required for using Spock with Maven.
- This module does *not* work with Maven 3+.

Maven 2.x Usage
---------------

Add the following plugin to your `pom.xml`:

```
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.spockframework</groupId>
        <artifactId>spock-maven</artifactId>
        <version>1.0-groovy-2.0-SNAPSHOT</version>
        <executions>
          <execution>
            <goals><goal>find-specs</goal></goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

That's it, run `mvn test` and your Specifications should be executed as tests.

Maven 3+ Usage
---------------

As mentioned above, the spock-maven plugin does not work with Maven 3+. But you can
run Spock Specifications as tests by configuring the
[Maven Surefire Plugin](http://maven.apache.org/surefire/maven-surefire-plugin/)
to also include the `**/*Spec.java` pattern in its search for tests to run.

Do this by adding the following plugins configuration:

```
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.16</version>
        <configuration>
          <useFile>false</useFile>
          <includes>
            <include>**/Test*.java</include>
            <include>**/*Test.java</include>
            <include>**/*TestCase.java</include>
            <include>**/*Spec.java</include>
          </includes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

Note: Even though you write your Spock specification classes in groovy files with names
matching the pattern `*Spec.groovy*`. Surefire matches on `**/*Spec.java`.


Using Spock with Maven
----------------------

To use Spock with maven, you need to add the following dependency to your `pom.xml`:

```
<project>
  <dependencies>
    <dependency>
      <groupId>org.spockframework</groupId>
      <artifactId>spock-core</artifactId>
      <version>0.7-groovy-2.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

And add the following plugin configuration:

```
<project>
  <build>
    <plugins>
      <plugin>
          <groupId>org.codehaus.gmaven</groupId>
          <artifactId>gmaven-plugin</artifactId>
          <version>1.4</version>
          <configuration>
              <providerSelection>2.0</providerSelection>
          </configuration>
          <executions>
              <execution>
                  <goals>
                      <goal>compile</goal>
                      <goal>testCompile</goal>
                  </goals>
              </execution>
          </executions>
          <dependencies>
              <dependency>
                  <groupId>org.codehaus.gmaven.runtime</groupId>
                  <artifactId>gmaven-runtime-2.0</artifactId>
                  <version>1.4</version>
                  <exclusions>
                      <exclusion>
                          <groupId>org.codehaus.groovy</groupId>
                          <artifactId>groovy-all</artifactId>
                      </exclusion>
                  </exclusions>
              </dependency>
              <dependency>
                  <groupId>org.codehaus.groovy</groupId>
                  <artifactId>groovy-all</artifactId>
                  <version>2.1.5</version>
              </dependency>
          </dependencies>
      </plugin>
    </plugins>
  </build>
</project>
```
