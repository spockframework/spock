package org.spockframework.smoke

import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.Rollup
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/1200")
class NameClashes extends Specification {
  @Rollup
  def 'getAt data variable in multi assignment does not clash with getAt method in data processor method'() {
    expect:
    true

    where:
    [getAt, foo] << [[_, _]]
  }

  def 'isInstance does not clash with rewritten instanceof condition'() {
    def isInstance = _
    expect:
    _ instanceof Object
  }

  def 'record does not clash with rewritten condition'() {
    def record = _
    expect:
    true
  }

  def 'startRecordingValue does not clash with rewritten condition'() {
    def startRecordingValue = _
    expect:
    true
  }

  def 'despreadList does not clash with rewritten condition'() {
    def despreadList = _
    expect:
    _.toString(*[])
  }

  def 'realizeNas does not clash with rewritten condition'() {
    def realizeNas = _
    expect:
    _.toString()
  }

  def 'reset does not clash with rewritten condition'() {
    def reset = _
    expect:
    true
  }

  def 'addInteraction does not clash with rewritten interaction'() {
    def addInteraction = _
    _ * _
    expect:
    true
  }

  def 'setRangeCount does not clash with rewritten interaction'() {
    def setRangeCount = _
    (0..Integer.MAX_VALUE) * _
    expect:
    true
  }

  def 'setFixedCount does not clash with rewritten interaction'() {
    def setFixedCount = _
    _ * _
    expect:
    true
  }

  def 'addEqualTarget does not clash with rewritten interaction'() {
    def addEqualTarget = _
    _ * _._()
    expect:
    true
  }

  def 'addWildcardTarget does not clash with rewritten interaction'() {
    def addWildcardTarget = _
    _ * _
    expect:
    true
  }

  def 'addEqualMethodName does not clash with rewritten interaction'() {
    def addEqualMethodName = _
    _ * _
    expect:
    true
  }

  def 'addRegexMethodName does not clash with rewritten interaction'() {
    def addRegexMethodName = _
    _ * _./.+/()
    expect:
    true
  }

  def 'addEqualPropertyName does not clash with rewritten interaction'() {
    def addEqualPropertyName = _
    _ * _._
    expect:
    true
  }

  def 'addRegexPropertyName does not clash with rewritten interaction'() {
    def addRegexPropertyName = _
    _ * _./.+/
    expect:
    true
  }

  def 'setArgListKind does not clash with rewritten interaction'() {
    def setArgListKind = _
    _ * _._()
    expect:
    true
  }

  def 'addArgName does not clash with rewritten interaction'() {
    def addArgName = _
    _ * _._(_: _)
    expect:
    true
  }

  def 'negateLastArg does not clash with rewritten interaction'() {
    def negateLastArg = _
    _ * _._(!_)
    expect:
    true
  }

  def 'typeLastArg does not clash with rewritten interaction'() {
    def typeLastArg = _
    _ * _._(_ as Object)
    expect:
    true
  }

  def 'addCodeArg does not clash with rewritten interaction'() {
    def addCodeArg = _
    _ * _._ {}
    expect:
    true
  }

  def 'addEqualArg does not clash with rewritten interaction'() {
    def addEqualArg = _
    _ * _._(_)
    expect:
    true
  }

  def 'addIterableResponse does not clash with rewritten interaction'() {
    def addIterableResponse = _
    _ >>> _
    expect:
    true
  }

  def 'addCodeResponse does not clash with rewritten interaction'() {
    def addCodeResponse = _
    _ >> {}
    expect:
    true
  }

  def 'addConstantResponse does not clash with rewritten interaction'() {
    def addConstantResponse = _
    _ >> _
    expect:
    true
  }

  def 'build does not clash with rewritten interaction'() {
    def build = _
    _ * _
    expect:
    true
  }

  def 'leaveScope does not clash with rewritten specification'() {
    def leaveScope = _
    expect:
    true
  }

  def 'enterScope does not clash with rewritten specification'() {
    def enterScope = _
    when:
    _.toString()
    then:
    _ * _
  }

  def 'addBarrier does not clash with rewritten specification'() {
    def addBarrier = _
    when:
    _.toString()
    then:
    _ * _
    then:
    _ * _
  }

  @FailsWith(Error)
  def 'addSuppressed does not clash with rewritten specification'() {
    def addSuppressed = _
    expect:
    throw new Error()
    cleanup:
    throw new Exception()
  }

  def 'getMockController does not clash with rewritten specification'() {
    def getMockController = _
    expect:
    true
  }

  def 'setThrownException does not clash with rewritten specification'() {
    def setThrownException = _
    when:
    throw new Exception()
    then:
    thrown(Exception)
  }

  @Rollup
  @FailsWith(Error)
  def 'validateCollectedErrors does not clash with rewritten specification'() {
    expect:
    true
    throw new Error()
    where:
    validateCollectedErrors = _
  }

  def 'getSpecificationContext does not clash with rewritten specification'() {
    def getSpecificationContext = _
    expect:
    true
  }
}
