package grails.plugin.spock.functional

import grails.plugin.spock.functional.test.TestHttpServer

import spock.lang.*

class WebSessionSpecification extends Specification {

  @Shared server
  @Shared session
  
  def setupSpeck() {
    server = new TestHttpServer()
    server.start()
    session = new WebSession(server.baseUrl)
  }

  def "simple get"() {
    setup:
    server.get = { req, res -> res.outputStream << "abc" }
    when:
    get("/")
    then:
    response.contentAsString == "abc"
  }
  
  def "get with params"() {
    setup:
    server.get = { req, res -> res.outputStream << req.getParameter('p1') + ' ' + req.getParameter('p2') }
    when:
    get("/") {
      p1 = p1Value
      p2 = p2Value
    }
    then:
    response.contentAsString == "$p1Value $p2Value"
    where:
    p1Value << ['1']
    p2Value << ['2']
  }
  
  def "get with headers"() {
    setup:
    server.get = { req, res -> res.outputStream << req.getHeader('p1') + ' ' + req.getHeader('p2') }
    when:
    def p1ValueLocal = p1Value
    def p2ValueLocal = p2Value
    get("/") {
      headers {
        p1 = p1ValueLocal
        p2 = p2ValueLocal
      }
    }
    then:
    response.contentAsString == "$p1Value $p2Value"
    where:
    p1Value << ['1']
    p2Value << ['2']
  }
  
  def "get with path"() {
    setup:
    server.get = { req, res -> res.outputStream << req.requestURI }
    when:
    get(path)
    then:
    response.contentAsString == uri
    where:
    path << ["path", "/path"]
    uri << ["/path", "/path"]
  }
  
  def "get with url"() {
    setup:
    def otherServer = new TestHttpServer()
    otherServer.start()
    otherServer.get = { req, res -> res.outputStream << "yes" }
    when:
    get(otherServer.baseUrl)
    then:
    response.contentAsString == "yes"
  }
  
  def "test don't follow redirect"() {
    setup:
    server.get = { req, res -> 
      if (req.requestURI == "/first") {
        res.sendRedirect("/second") 
      } else {
        res.outputStream << "yes"
      }
    }
    when:
    redirectEnabled = false
    then:
    redirectEnabled == false
    when:
    get("/first")
    then:
    didReceiveRedirect
    redirectURL.endsWith("/second")
    when: 
    followRedirect() != null
    then:
    response.contentAsString == "yes"
    didReceiveRedirect == false
    when:
    redirectEnabled = true
    redirectEnabled == true
    get("/first")
    then:
    response.contentAsString == "yes"
    followRedirect() == null
  }
  
  def cleanupSpeck() {
    server.stop()
  }
  
  def methodMissing(String name, args) {
    session."$name"(*args)
  }
  
  def propertyMissing(String name) {
    session."$name"
  }
  
  def propertyMissing(String name, value) {
    session."$name" = value
  }
}