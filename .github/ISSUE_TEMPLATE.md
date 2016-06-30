># Help us help you
>We are glad you are reporting your issue.
>
>Please follow these guidelines when so we better can classify the issue.
>
> The more info you provide about your environment and how to reproduce the problem the easier and faster it will be to solve (win-win).
>
>Please check existing issues (both open and closed) prior to opening any new issue.
>Optionally make sure it is a bug by other users in [forum](http://forum.spockframework.org), [StackOverflow Spock questions](http://stackoverflow.com/questions/tagged/spock), etc.
>
> The block quotes can be omitted.
> Most of the sections are only applicable for bugs.

# Issue description
> A clear and concise description.
> Make sure you choose the correct label as well (bug or enhancement)

# How to reproduce
> Please provide information how to reproduce this issue.
> Only applicable for bugs.

## Link to a gist or similar (optional)

# Additional Environment information
> Version of your build tool(if used), Java, Groovy, IDE, OS etc

## Java/JDK
`java -version`

## Groovy version
>__Note that versions older than 2.0 are no longer supported.__

`groovy -version`

## Build tool version

### Gradle
`gradle -version`

### Apache Maven
`mvn -version`

## Operating System
> Linux, Windows, Mac etc.

## IDE
> IntelliJ, Eclipse etc.

## Build-tool dependencies used

### Gradle/Grails
    compile 'org.spockframework:spock-core:1.1-groovy-2.4-rc-1'

### Apache Maven
    <dependency>
      <groupId>org.spockframework</groupId>
      <artifactId>spock-core</artifactId>
      <version>1.1-groovy-2.4-rc-1</version>
    </dependency>

### Groovy Grape
    @Grapes(
      @Grab(group='org.spockframework', module='spock-core', version='1.1-groovy-2.4-rc-1')
    )

### Scala SBT
    libraryDependencies += "org.spockframework" % "spock-core" % "1.1-groovy-2.4-rc-1"


