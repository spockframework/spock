import grails.plugin.spock.*

class TestFunctionalSpecification extends FunctionalSpecification {

  def simpleService
  
  def 'simple controller'() {
    when:
    simpleService.name = "something"
    get("/simple")
    
    then:
    response.contentAsString == 'something'
  }

}