import grails.plugin.spock.*

class TestTagLibSpecification extends TagLibSpecification {

  def 'bar tag'() {
    expect: 
    bar() == "<p>Hello World!</p>"
  }
  
  def 'bar tag subsequent call'() {
    expect: 
    bar() == "<p>Hello World!</p>"
    bar() == "<p>Hello World!</p>"
  }
  
  def 'body tag'() {
    expect: 
    bodyTag(name: 'a') { "Foo" } == "<a>Foo</a>"
    bodyTag(name: 'b') { "Bar" } == "<b>Bar</b>"
  }
  
}


class TestTagLib {
  def bar = { attrs, body ->
    out << "<p>Hello World!</p>"
  }

  def bodyTag = { attrs, body ->
    out << "<${attrs.name}>"
    out << body()
    out << "</${attrs.name}>"
  }
}