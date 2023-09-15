package org.spockframework.mock

import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Nullable
import spock.lang.Issue
import spock.lang.Specification

import java.lang.annotation.Annotation

class MockSpecInfoAnnotationSpec extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/1163")
  def "NodeInfo.getAnnotation() shall return valid Stub for Stub Issue #1163"() {
    given:
    def mockUtil = new MockUtil()
    def spec = Stub(SpecInfo)

    when:
    def t = spec.getAnnotation(Nullable)

    then:
    t instanceof Annotation
    mockUtil.isMock(t)
  }

  @Issue("https://github.com/spockframework/spock/issues/1163")
  def "NodeInfo.getAnnotation() shall return null for Mock Issue #1163"() {
    given:
    def spec = Mock(SpecInfo)

    when:
    def t = spec.getAnnotation(Nullable)

    then:
    t == null
  }

  @Issue("https://github.com/spockframework/spock/issues/520")
  def "Better support for generic return types with Stub() Issue #520"() {
    when:
    def stub = Stub(Issue520Repository)
    then:
    stub.persist(null) instanceof Serializable
  }
}
