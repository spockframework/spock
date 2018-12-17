package org.spockframework.junit4.junit

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder

import spock.lang.Shared
import spock.lang.Specification

class UseJUnitClassRule extends Specification {
  @ClassRule
  @Shared TemporaryFolder folder1 = new TemporaryFolder()

  @ClassRule
  static TemporaryFolder folder2 = new TemporaryFolder()

  @Shared File root1
  @Shared File root2

  def setupSpec() {
    root1 = folder1.root
    //root2 = folder2.root

    assert root1.exists()
    //assert root2.exists()
  }

  def feature() {
    expect:
    root1 == folder1.root
    //root2 == folder2.root

    root1.exists()
    //root2.exists()
  }

  def cleanupSpec() {
    assert root1 == folder1.root
    //assert root2 == folder2.root

    assert root1.exists()
    //assert root2.exists()
  }
}
