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

package grails.plugin.spock

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.test.support.GrailsTestAutowirer
import org.codehaus.groovy.grails.test.support.GrailsTestTransactionInterceptor
import org.codehaus.groovy.grails.test.support.GrailsTestRequestEnvironmentInterceptor

import spock.lang.Specification

class IntegrationSpec extends Specification {
  private transactionManager
  private transactionStatus

  private applicationContext = ApplicationHolder.application.mainContext
  private autowirer = new GrailsTestAutowirer(applicationContext)
  private transactionInterceptor = new GrailsTestTransactionInterceptor(applicationContext)
  private requestEnvironmentInterceptor = new GrailsTestRequestEnvironmentInterceptor(applicationContext)

  def setup() {
    autowirer.autowire(this)
    requestEnvironmentInterceptor.init()
    if (transactionInterceptor.isTransactional(this)) transactionInterceptor.init()
  }

  def cleanup() {
    transactionInterceptor.destroy()
    requestEnvironmentInterceptor.init()
  }
}