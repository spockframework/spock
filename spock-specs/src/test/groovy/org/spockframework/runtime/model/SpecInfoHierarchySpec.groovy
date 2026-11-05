package org.spockframework.runtime.model

import org.spockframework.runtime.SpecInfoBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.EmbeddedSpecCompiler

class SpecInfoHierarchySpec extends Specification {

  @Shared def grandparent
  @Shared def parent
  @Shared def middle
  @Shared def child
  @Shared def grandchild

  @Shared List hierarchy
  @Shared hierarchyNames = ["grandparent", "parent", "middle", "child", "grandchild"]

  def setupSpec() {
    def specs = specsTopToBottom
    grandparent = specs[0]
    parent = specs[1]
    middle = specs[2]
    child = specs[3]
    grandchild = specs[4]
    hierarchy = [grandparent, parent, middle, child, grandchild]
  }

  Tuple5 getSpecsTopToBottom() {
    def classes = new EmbeddedSpecCompiler().compile("""import spock.lang.Specification
class Grandparent extends  Specification {}
class Parent extends Grandparent {}
class Middle extends Parent {}
class Child extends Middle {}
class Grandchild extends Child {}
""")

    def grandchildClass = classes.find { it.simpleName == 'Grandchild' }
    SpecInfo specInfo = new SpecInfoBuilder(grandchildClass).build()
    return specInfo.getSpecsTopToBottom() as Tuple5
  }

  @Unroll("#name")
  def "top spec"(SpecInfo underTest) {
    expect:
    underTest.getTopSpec() == grandparent

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "bottom spec"(SpecInfo underTest) {
    expect:
    underTest.getBottomSpec() == grandchild

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs top to bottom"(SpecInfo underTest) {
    expect:
    underTest.specsTopToBottom == hierarchy

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs bottom to top"(SpecInfo underTest) {
    expect:
    underTest.specsBottomToTop == hierarchy.reverse()

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs to bottom"(SpecInfo underTest) {
    expect:
    underTest.specsCurrentToBottom == hierarchy[index..-1]

    where:
    underTest << hierarchy
    name << hierarchyNames
    index << (0..4)
  }

  @Unroll("#name")
  def "specs to top"(SpecInfo underTest) {
    expect:
    underTest.specsCurrentToTop == hierarchy[index..0]

    where:
    underTest << hierarchy
    name << hierarchyNames
    index << (-5..-1)
  }

}
