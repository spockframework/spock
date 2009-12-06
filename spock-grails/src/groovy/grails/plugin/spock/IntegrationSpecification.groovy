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

import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

import org.codehaus.groovy.grails.test.support.GrailsTestAutowirer
import org.codehaus.groovy.grails.test.support.GrailsTestTransactionInterceptor
import org.codehaus.groovy.grails.test.support.GrailsTestRequestEnvironmentInterceptor

class IntegrationSpecification extends Specification {
  private transactionManager
  private transactionStatus

  private applicationContext = ApplicationHolder.application.mainContext
  private autowirer = new GrailsTestAutowirer(applicationContext)
  private transactionInterceptor = new GrailsTestTransactionInterceptor(applicationContext)
  private requestEnvironmentInterceptor = new GrailsTestRequestEnvironmentInterceptor(applicationContext)

  def setup() {
    autowirer.autowire(this)
    
    // Once GrailsTestRequestEnvironmentInterceptor supports init(), it should replace these two lines
    def webRequest = GrailsWebUtil.bindMockWebRequest(applicationContext)
    webRequest.servletContext.setAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT, applicationContext)

    if (transactionInterceptor.isTransactional(this)) {
      // Once GrailsTestTransactionInterceptor supports init(), it should replace the next eight lines
      if (applicationContext.containsBean("transactionManager")) {
        transactionManager = applicationContext.getBean("transactionManager")
        transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition())
      } else {
        throw new RuntimeException("There is no test TransactionManager defined and integration test" +
            "${this.class.name} does not set transactional = false"
        )
      }
    }
  }

  def cleanup() {
    // Once GrailsTestTransactionInterceptor supports rollback(), it should replace the next line
    transactionManager?.rollback(transactionStatus)
    // Once GrailsTestRequestEnvironmentInterceptor supports destroy(), it should replace the next line
    RequestContextHolder.requestAttributes = null
  }
}