import grails.plugin.spock.*

class TestControllerSpecification extends ControllerSpecification {

  def 'text action'() {
    when:
    controller.text()
    
    then:
    mockResponse.contentAsString == "text"
  }

  def 'some redirect action'() {
    when:
    controller.someRedirect()
    
    then:
    redirectArgs == [action: "someRedirect"]
  }
  
  def 'bodyElementText action'() {
    when:
    xmlRequestContent = requestContent
    
    and:
    controller.bodyElementText()
    
    then:
    mockResponse.contentAsString == value
    
    where:
    value << ['value', 'value']
    requestContent << ["<request><body>value</body></request>", { request { body('value') } }]
  }

  def 'model action'() {
    expect:
    controller.model() == [a: '1']
  }
}

class TestController {
  def text = {
    render "text"
  }

  def someRedirect = {
    redirect(action: "someRedirect")
  }
  
  def bodyElementText = {
    render request.'XML'.body.text()
  }

  def model = {
    [a: '1']
  }
}