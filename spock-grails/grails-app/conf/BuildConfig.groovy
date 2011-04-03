/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
  inherits "global" // inherit Grails' default dependencies
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

  repositories {
    grailsHome()
    grailsCentral()
    mavenLocal() // prefer local, so we pick up spock snapshot as part of whole build
    mavenRepo "http://m2repo.spockframework.org/snapshots"
    mavenCentral()
  }
  
  dependencies {
    // If we are running as part of spock build, don't try and pull in
    // as gradle sets up the deps for us. This prop is set in the gradle build.
    if (System.getProperty("spock.building") == null) {
      def spockVersion = "0.6"
      def groovyVersion = grailsVersion.startsWith("1.3") ? "1.7" : "1.8"
      def isSnapshot = true

      def effectiveSpockVersion = "${spockVersion}-groovy-${groovyVersion}"
      if (isSnapshot) {
        effectiveSpockVersion += "-SNAPSHOT"
      }

      test("org.spockframework:spock-grails-support:${effectiveSpockVersion}") {
        exclude "groovy-all"
      }
    }
  }
  
  plugins {
    compile (":tomcat:$grailsVersion", ":hibernate:$grailsVersion") {
      export = false
    }
  }
}
