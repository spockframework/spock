import spock.lang.*

class BadSpecification extends Specification {
  def "intentionally failing"() {
    expect:
    name.size() == size

    where:
    name << ["Kirk", "Spock", "Scott"]
    size << [4, 5, 6]
  }
  
  def "intentionally failing2"() {
    expect:
    name.size() == size

    where:
    name << ["Kirk", "Spock", "Scott"]
    size << [4, 5, 6]
  }
  
}