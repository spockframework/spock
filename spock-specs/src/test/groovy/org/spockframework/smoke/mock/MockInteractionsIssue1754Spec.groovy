package org.spockframework.smoke.mock

import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/pull/1754")
class MockInteractionsIssue1754Spec extends Specification {

  def "When block interaction still active after when block without verification"() {
    given:
    def m = Mock(Engine)
    when:
    m.isStarted() >> true
    def result = m.isStarted()
    m.start()
    then:
    m.isStarted()
    result
    when: "Here the isStarted() still reflect the when block above"
    result = m.isStarted()
    then:
    m.isStarted()
    result
  }

  def "When block interactions shall be preserved also if there are then interactions"() {
    given:
    def m = Mock(Engine)
    when:
    m.isStarted() >> true
    def result = m.isStarted()
    m.start()
    then:
    1 * m.start()
    m.isStarted()
    result
    when:
    result = m.isStarted()
    then:
    m.isStarted()
    result
  }

  def "then block is last block in feature"() {
    given:
    def m = Mock(Engine)
    when:
    m.isStarted() >> true
    def result = m.isStarted()
    m.start()
    then:
    result
    m.isStarted()
    1 * m.start()
  }

  def "When block interactions with spread over overlapping ordered interactions"() {
    def list = Mock(List)

    when:
    list.size() >> 2
    list.add(1)
    list.add(2)
    list.add(2)
    list.add(1)
    assert list.size() == 2

    then:
    1 * list.add(!0)
    list.size() == 2

    then:
    2 * list.add(2)
    list.size() == 2

    then:
    1 * list.add(_)
    list.size() == 2
  }

  static class Engine {
    private boolean started

    boolean isStarted() { return started }

    void start() { started = true }
  }
}
