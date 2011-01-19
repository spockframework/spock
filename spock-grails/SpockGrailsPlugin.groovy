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

class SpockGrailsPlugin {
  def version = "0.6-groovy-1.7-SNAPSHOT"
  def grailsVersion = "1.3.0 > *"
  def dependsOn = [:]
  def pluginExcludes = [
      "grails-app/**",
      "web-app"
  ]

  def author = "Luke Daley"
  def authorEmail = "ld@ldaley.com"
  def title = "Spock Plugin - spockframework.org"
  def description = 'Test your Grails apps with Spock'
  def documentation = 'http://grails.org/plugin/spock'
}
