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
import org.codehaus.groovy.grails.test.support.ControllerNameExtractor

import grails.plugin.spock.test.GrailsSpecTestType

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Stepwise

class IntegrationSpec extends Specification {
  @Shared private applicationContext = ApplicationHolder.application.mainContext
  @Shared private autowirer = new GrailsTestAutowirer(applicationContext)
  
  @Shared private perSpecTransactionInterceptor
  @Shared private perSpecRequestEnvironmentInterceptor
  
  private perMethodTransactionInterceptor = null
  private perMethodRequestEnvironmentInterceptor = null

  def setupSpec() {
    if (isStepwiseSpec()) {
      perSpecTransactionInterceptor = initTransaction()
      perSpecRequestEnvironmentInterceptor = initRequestEnv()
    }
    
    autowirer.autowire(this)
  }
  
  def setup() {
    if (!isStepwiseSpec()) {
      perMethodTransactionInterceptor = initTransaction()
      perMethodRequestEnvironmentInterceptor = initRequestEnv()
    }
    
    autowirer.autowire(this)
  }

  def cleanup() {
    perMethodRequestEnvironmentInterceptor?.destroy()
    perMethodTransactionInterceptor?.destroy()
  }
  
  def cleanupSpec() {
    perSpecRequestEnvironmentInterceptor?.destroy()
    perSpecTransactionInterceptor?.destroy()
  }
  
  private boolean isStepwiseSpec() {
    getClass().isAnnotationPresent(Stepwise)
  }

  private initTransaction() {
    def interceptor = new GrailsTestTransactionInterceptor(applicationContext)
    if (interceptor.isTransactional(this)) interceptor.init()
    interceptor
  }
  
  private initRequestEnv() {
    def interceptor = new GrailsTestRequestEnvironmentInterceptor(applicationContext)
    def controllerName = ControllerNameExtractor.extractControllerNameFromTestClassName(
        this.class.name, GrailsSpecTestType.TEST_SUFFIXES as String[])
    interceptor.init(controllerName ?: GrailsTestRequestEnvironmentInterceptor.DEFAULT_CONTROLLER_NAME)
    interceptor
  }
}