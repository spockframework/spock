import grails.plugin.spock.*

class TestTagLibSpecification extends TagLibSpecification {

  def 'bar tag'() {
    expect: 
    bar(p, b) == o
    
    where:
    p << [null, [a: 1]]
    b << [null, null]
    o << ["<p>Hello World!</p>", "<p>Hello World!</p>"]
  }

  def 'bar tag subsequent call'() {
    expect: 
    bar() == "<p>Hello World!</p>"
    bar() == "<p>Hello World!</p>"
  }
  
  def 'body tag'() {
    expect:
    bodyTag(p, b) == o
    
    where: 
    p << [[name: 'a'], [name: 'b']]
    b << [{ "Foo" }, { "Bar" }]
    o << ["<a>Foo</a>", "<b>Bar</b>"]
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