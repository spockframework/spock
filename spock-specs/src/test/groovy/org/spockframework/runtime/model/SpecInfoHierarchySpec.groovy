package org.spockframework.runtime.model

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SpecInfoHierarchySpec extends Specification {

  @Shared def grandparent = new SpecInfo()
  @Shared def parent = new SpecInfo()
  @Shared def specInfo = new SpecInfo()
  @Shared def child = new SpecInfo()
  @Shared def grandchild = new SpecInfo()

  @Shared hierarchy = [grandparent, parent, specInfo, child, grandchild]
  @Shared hierarchyNames = ["grandparent", "parent", "specInfo", "child", "grandchild"]

  def setupSpec() {
    parent.superSpec = grandparent
    grandparent.subSpec = parent

    specInfo.superSpec = parent
    parent.subSpec = specInfo

    child.superSpec = specInfo
    specInfo.subSpec = child

    grandchild.superSpec = child
    child.subSpec = grandchild
  }

  @Unroll("#name")
  def "top spec"() {
    expect:
    underTest.getTopSpec() == grandparent

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "bottom spec"() {
    expect:
    underTest.getBottomSpec() == grandchild

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs top to bottom"() {
    expect:
    underTest.specsTopToBottom == hierarchy

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs bottom to top"() {
    expect:
    underTest.specsBottomToTop == hierarchy.reverse()

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs to bottom"() {
    expect:
    underTest.specsToBottom == hierarchy.subList(hierarchy.indexOf(underTest), hierarchy.size())

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

  @Unroll("#name")
  def "specs to top"() {
    def reversedHierarchy = hierarchy.reverse()

    expect:
    underTest.specsToTop == reversedHierarchy.subList(reversedHierarchy.indexOf(underTest), reversedHierarchy.size())

    where:
    underTest << hierarchy
    name << hierarchyNames
  }

}
