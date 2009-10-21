import grails.plugin.spock.IntegrationSpecification
 
class TestIntegrationSpecification extends IntegrationSpecification {

    def simpleService
    
    void setup() {
        super.setup() // Spock currently require
    }
    
    def "injected simpleService is present"() {
      expect:
      simpleService.name = input
      simpleService.name == input

      where:
      input << ["bob"]
    }
    
    def "can write to database"() {
        expect:
        def p = new Person(name: 'n').save() != null
        Person.findByName('n') != null
    }

}