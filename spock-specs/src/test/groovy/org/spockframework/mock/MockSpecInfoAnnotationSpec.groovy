package org.spockframework.mock

import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Nullable
import spock.lang.Specification

import java.lang.annotation.Annotation

class MockSpecInfoAnnotationSpec extends Specification {

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

  def "NodeInfo.getAnnotation() shall return null for Mock Issue #1163"() {
    given:
    def spec = Mock(SpecInfo)

    when:
    def t = spec.getAnnotation(Nullable)

    then:
    t == null
  }
}
