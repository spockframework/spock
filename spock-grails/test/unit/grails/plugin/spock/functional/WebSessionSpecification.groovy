package grails.plugin.spock.functional

import grails.plugin.spock.functional.test.TestHttpServer

import spock.lang.*

class WebSessionSpecification extends Specification {

  @Shared server
  @Shared session

  @Shared simpleRedirect = { req, res -> 
    if (req.requestURI == "/first") {
      res.sendRedirect("/second") 
    } else {
      res.outputStream << "yes"
    }
  }
  
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
    server.get = simpleRedirect
    when:
    redirectEnabled = false
    then:
    redirectEnabled == false
    when:
    get("/first")
    then:
    didReceiveRedirect
    redirectURL.endsWith("/second")
    followRedirect() != null
    response.contentAsString == "yes"
    didReceiveRedirect == false
    when:
    redirectEnabled = true
    then:
    redirectEnabled == true
    when:
    get("/first")
    then:
    response.contentAsString == "yes"
    followRedirect() == null
  }
  
  def "redirects are followed by default"() {
    setup:
    server.get = simpleRedirect
    when:
    get("/first")
    then:
    response.contentAsString == "yes"
  }
  
  def "page return"() {
    setup:
    server.get = { req, res -> res.outputStream << "abc" }
    expect:
    get("/").webResponse.contentAsString == "abc"
  }
  
  def "cookie tests"() {
    setup:
    server.get = { req, res -> 
      res.addCookie(new javax.servlet.http.Cookie(cookieName, cookieValue))
    }
    when:
    get("/")
    then:
    cookies.find { it.name == cookieName}.value == cookieValue
    when:
    cookieManager.clearCookies()
    then:
    cookies.size() == 0
    where:
    cookieName << ["name"]
    cookieValue << ["value"]
  }
  
  def "form tests"() {
    setup:
    server.get = { req, res ->
      if (req.requestURI == "/") {
        res.outputStream << """
        <html>
        <body>
        	<form name="form" action="/somewhereelse">
        		<input type="text" name="textInput" value="textInputValue" />

        		<textarea name="textarea" rows="8" cols="40">textareaValue</textarea>

        		<input type="radio" name="group1" value="group1Value1"/>
        		<input type="radio" name="group1" value="group1Value2" checked/>

        		<input type="radio" name="group2" value="group2Value1"/>
        		<input type="radio" name="group2" value="group2Value2" checked/>

        		<select name="select">
        			<option value="option1Value">option1Display</option>
        			<option value="option2Value" selected>option2Display</option>
        		</select>

        		<input type="submit" name="submitInput" value="submitInputValue" />
        	</form>
        </body>
        </html>"""
      } else {
        res.outputStream << ['textInput', 'textarea', 'group1', 'group2', 'select'].collect { req.getParameter(it)}.join(':')

      }
    }
    when:
    get("/")
    
    then:
    form() != null
    form {} != null
    form(0) {} != null
    form('form') != null
    form('form') {} != null
    when:
    def f = form()
    then:
    f.textInput == "textInputValue"
    f.textarea == "textareaValue"
    f.group1 == "group1Value2"
    f.group2 == "group2Value2"
    f.select == ["option2Value"]
    
    form().with { // have to use with here, spock barfs otherwise
      textInput = "textInputValueChanged"
      textarea = "textareaValueChanged"
      group1 = "group1Value1"
      group2 = "group2Value1"
      select = "option1Value"
    }
    
    f.textInput == "textInputValueChanged"
    f.textarea == "textareaValueChanged"
    f.group1 == "group1Value1"
    f.group2 == "group2Value1"
    f.select == ["option1Value"]
    when:
    form {
      click 'submitInput'
    }
    then:
    response.contentAsString == 'textInputValueChanged:textareaValueChanged:group1Value1:group2Value1:option1Value'
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