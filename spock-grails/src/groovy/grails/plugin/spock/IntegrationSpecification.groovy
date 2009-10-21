package grails.plugin.spock

import spock.lang.Specification

import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.web.context.GrailsConfigUtils

import org.springframework.context.ApplicationContextAware
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.context.request.RequestContextHolder

class IntegrationSpecification extends Specification {

    private transactionManager
    private transactionStatus
    
    void setupSpeck() {}
    
    void setup() {
        def applicationContext = ApplicationHolder.application.mainContext
        assert applicationContext != null
        
        applicationContext.autowireCapableBeanFactory.autowireBeanProperties(
            this,
            AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
            false
        )
        
        if (this instanceof ApplicationContextAware) {
            this.applicationContext = applicationContext
        }
        
        
        def webRequest = GrailsWebUtil.bindMockWebRequest(applicationContext);
        webRequest.servletContext.setAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT, applicationContext)

        GrailsConfigUtils.executeGrailsBootstraps(webRequest.attributes.grailsApplication, applicationContext, webRequest.servletContext)
        
        if (GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(this, "transactional")) {
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
    
    void cleanup() {
        transactionManager?.rollback(transactionStatus)
        RequestContextHolder.requestAttributes = null
    }

    void cleanupSpeck() {}
}