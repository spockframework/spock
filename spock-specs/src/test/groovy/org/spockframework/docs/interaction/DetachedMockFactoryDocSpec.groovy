package org.spockframework.docs.interaction

import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockUtil
import org.spockframework.mock.ZeroOrNullResponse
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.AutoAttach
import spock.mock.DetachedMockFactory

import java.util.concurrent.ThreadLocalRandom

import static org.spockframework.docs.interaction.DetachedMockFactoryDocSpec.EngineMockCreator.StartMode.*

class DetachedMockFactoryDocSpec extends Specification {
  // tag::declare-shared[]
  @Shared 
  def mockFactory = new DetachedMockFactory()
  
  @Shared 
  def mockUtil = new MockUtil()
  // end::declare-shared[]

  // tag::attach-manually[]
  def "Manually attach detached mock"() {
    given:
    def manuallyAttachedEngine = mockFactory.Mock(Engine)
    mockUtil.attachMock(manuallyAttachedEngine, this)
    manuallyAttachedEngine.isStarted() >> true
    def car = new Car(engine: manuallyAttachedEngine)

    when:
    car.drive()
    then:
    1 * manuallyAttachedEngine.start()
    manuallyAttachedEngine.isStarted()

    when:
    car.park()
    then:
    1 * manuallyAttachedEngine.stop()
    manuallyAttachedEngine.isStarted()

    cleanup:
    mockUtil.detachMock(manuallyAttachedEngine)
  }
  // end::attach-manually[]

  // tag::auto-attach[]
  @AutoAttach
  def autoAttachedEngine = mockFactory.Mock(Engine)

  def "Auto-attach detached mock"() {
    given:
    autoAttachedEngine.isStarted() >> true
    def car = new Car(engine: autoAttachedEngine)

    when:
    car.drive()
    then:
    1 * autoAttachedEngine.start()
    autoAttachedEngine.isStarted()

    when:
    car.park()
    then:
    1 * autoAttachedEngine.stop()
    autoAttachedEngine.isStarted()
  }
  // end::auto-attach[]

  // tag::use-custom-mock-creator-no-attach[]
  @Unroll("Engine state #engineStateResponseType")
  def "Mock usage without manually attach detach with preconfigured engine state"() {
    given:
    def car = new Car(engine: preconfiguredEngine)
    // The preconfigured mock with default behaviour behaves as defined,
    // even *without* attaching it to the spec.

    when:
    car.drive()
    then:
    possibleResponsesAfterStart.contains(preconfiguredEngine.isStarted())

    when:
    car.park()
    then:
    possibleResponsesAfterStop.contains(preconfiguredEngine.isStarted())

    where:
    engineStateResponseType | possibleResponsesAfterStart | possibleResponsesAfterStop
    ALWAYS_STARTED          | [true]                      | [true]
    ALWAYS_STOPPED          | [false]                     | [false]
    RANDOMLY_STARTED        | [true, false]               | [true, false]
    REAL_RESPONSE           | [true]                      | [false]
    preconfiguredEngine = EngineMockCreator.getMock(engineStateResponseType)
  }
  // end::use-custom-mock-creator-no-attach[]


  // tag::use-custom-mock-creator-attach[]
  @Unroll("Engine state #engineStateResponseType")
  def "Manually attach detached mock with preconfigured engine state"() {
    given:
    def car = new Car(engine: preconfiguredEngine)
    //Now, let's attach the mock to the spec and override its default behaviour.
    mockUtil.attachMock(preconfiguredEngine, this)
    preconfiguredEngine.isStarted() >> true

    expect:
    preconfiguredEngine.isStarted()
    // The attached mock now behaves differently. Because it has been attached to the
    // spec, we can also verify interactions using '1 * ...' or similar, which
    // would not be possible without attaching it.

    when:
    car.drive()
    then:
    1 * preconfiguredEngine.start()
    preconfiguredEngine.isStarted()

    when:
    car.park()
    then:
    1 * preconfiguredEngine.stop()
    preconfiguredEngine.isStarted()

    cleanup:
    mockUtil.detachMock(preconfiguredEngine)

    where:
    engineStateResponseType | possibleResponsesAfterStart | possibleResponsesAfterStop
    ALWAYS_STARTED          | [true]                      | [true]
    ALWAYS_STOPPED          | [false]                     | [false]
    RANDOMLY_STARTED        | [true, false]               | [true, false]
    REAL_RESPONSE           | [true]                      | [false]
    preconfiguredEngine = EngineMockCreator.getMock(engineStateResponseType)
  }
  // end::use-custom-mock-creator-attach[]

  static
  // tag::engine[]
  class Engine {
    private boolean started

    boolean isStarted() { return started }
    void start() { started = true }
    void stop() { started = false }
  }

  // end::engine[]

  static
  // tag::car[]
  class Car {
    private Engine engine

    void drive() { engine.start() }
    void park() { engine.stop() }
  }
  // end::car[]

  static
  // tag::custom-mock-creator[]
  class EngineMockCreator {
    enum StartMode {
      ALWAYS_STARTED, ALWAYS_STOPPED, RANDOMLY_STARTED, REAL_RESPONSE
    }

    static DetachedMockFactory mockFactory = new DetachedMockFactory()

    static class EngineStateResponse implements IDefaultResponse {
      StartMode startMode

      @Override
      Object respond(IMockInvocation invocation) {
        if (invocation.method.name != 'isStarted')
          return ZeroOrNullResponse.INSTANCE.respond(invocation)
        startMode == RANDOMLY_STARTED
          ? ThreadLocalRandom.current().nextBoolean()
          : startMode == ALWAYS_STARTED
      }
    }

    static Engine getMock(StartMode startMode) {
      startMode == REAL_RESPONSE
        ? mockFactory.Spy(new Engine())
        : mockFactory.Mock(Engine, defaultResponse: new EngineStateResponse(startMode: startMode)) as Engine
    }
  }
  // end::custom-mock-creator[]
}
