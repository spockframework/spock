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
  
  // NOTE - needs to kept in sync with version in Gradle build
  def spockVersion = "0.6"
  def groovyVersion = "1.8"
  def isSnapshot = true
  
  def effectiveSpockVersion = "${spockVersion}-groovy-${groovyVersion}"
  if (isSnapshot) effectiveSpockVersion += "-SNAPSHOT"

  def isSpockBuild = System.getProperty("spock.building") != null
  def isGrails14 = grailsVersion.startsWith("1.4")
  
  inherits "global" // inherit Grails' default dependencies
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

  repositories {
    grailsHome()
    grailsCentral()
    mavenCentral()
    if (isSnapshot) {
      mavenLocal()
      mavenRepo "http://m2repo.spockframework.org/snapshots"
    }
  }
  
  dependencies {
    test("org.spockframework:spock-grails-support:${effectiveSpockVersion}") {
      transitive = false
    }
    test("org.spockframework:spock-core:${effectiveSpockVersion}") {
      excludes "groovy-all"
    }
    test("hsqldb:hsqldb:1.8.0.7") {
      export = false
    }
  }
  
  plugins {
    test(":hibernate:$grailsVersion") {
      export = false
    }
    build(":release:1.0.0.M2") {
      export = false
    }
    runtime(":svn:1.0.0.M1") {
      export = false
    }
  }
}

grails.release.scm.enabled = false