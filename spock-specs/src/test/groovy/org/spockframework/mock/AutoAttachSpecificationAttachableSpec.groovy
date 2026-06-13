package org.spockframework.mock

import spock.mock.SpecificationAttachable
import spock.lang.Specification
import spock.mock.AutoAttach
import spock.mock.DetachedMockFactory

class AutoAttachSpecificationAttachableSpec extends Specification {

  static class RecordingAttachable implements SpecificationAttachable {
    Specification attached
    int detachCount = 0

    @Override
    void attach(Specification specification) { attached = specification }

    @Override
    void detach() { detachCount++; attached = null }
  }

  @AutoAttach
  RecordingAttachable fixture = new RecordingAttachable()

  @AutoAttach
  SpecificationAttachable detachedMock = new DetachedMockFactory().Mock(SpecificationAttachable)

  def "AutoAttach attaches a SpecificationAttachable field to the running spec during setup"() {
    expect:
    fixture.attached.is(this)
  }

  def "a mock whose type implements SpecificationAttachable is attached as a mock, not via attach()"() {
    when:
    detachedMock.detach()

    then: "the interaction is enforced on this spec, proving the mock was attached as a mock"
    1 * detachedMock.detach()
  }
}
