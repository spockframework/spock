package org.spockframework.junit.scheduling

import org.junit.experimental.ParallelComputer
import org.junit.internal.requests.FilterRequest
import org.junit.runner.Computer
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.notification.RunListener
import org.junit.runners.AllTests
import spock.lang.ConcurrentExecutionMode
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.EmbeddedSpecRunner

class SchedulingTest extends Specification {

  @Unroll
  def "with sequential scheduler iterations and features should be executed sequentially [#clazz]"(Class clazz) {
    def listener = Mock(RunListener)
    def jUnitCore = new JUnitCore()
    jUnitCore.addListener(listener)
    when:
    def runner = new EmbeddedSpecRunner()
    runner.withNewContext {
      jUnitCore.run(Computer.serial(), clazz);
    }
    then:
    1 * listener.testRunStarted(_)
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=1"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=1"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=2"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=2"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test simple"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test simple"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test no unroll"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test no unroll"))
    then:
    1 * listener.testRunFinished(_)

    where:
    clazz << [SampleTest.class, SampleTestWithTimeout.class]
  }

  @Unroll
  def "with parallel scheduler iterations and features should be executed concurrently [#clazz]"() {
    def listener = Mock(RunListener)
    def jUnitCore = new JUnitCore()
    jUnitCore.addListener(listener)
    when:
    def runner = new EmbeddedSpecRunner()
    runner.withNewContext {
      jUnitCore.run(ParallelComputer.methods(), clazz);
    }
    then:
    1 * listener.testRunStarted(_)
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=1"))
    1 * listener.testStarted(testDescription(clazz, "test for x=2"))
    1 * listener.testStarted(testDescription(clazz, "test simple"))
    1 * listener.testStarted(testDescription(clazz, "test no unroll"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=1"))
    1 * listener.testFinished(testDescription(clazz, "test for x=2"))
    1 * listener.testFinished(testDescription(clazz, "test simple"))
    1 * listener.testFinished(testDescription(clazz, "test no unroll"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testRunFinished(_)

    where:
    clazz << [SampleTest.class, SampleTestWithTimeout.class]
  }

  @Unroll
  def "with parallel scheduler iterations and features should be executed sequentially if FORCE_SEQUENTIAL for all class"() {
    def listener = Mock(RunListener)
    def jUnitCore = new JUnitCore()
    jUnitCore.addListener(listener)

    def clazz = SampleTestWithSequentialOnClassLevel.class
    when:
    def runner = new EmbeddedSpecRunner()
    runner.withNewContext {
      jUnitCore.run(ParallelComputer.methods(), clazz);
    }
    then:
    1 * listener.testRunStarted(_)
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=1"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=1"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=2"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=2"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test simple"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test simple"))
    then:
    1 * listener.testStarted(testDescription(clazz, "test no unroll"))
    then:
    1 * listener.testFinished(testDescription(clazz, "test no unroll"))
    then:
    1 * listener.testRunFinished(_)
  }

  @Unroll
  def "with parallel scheduler iterations and features should be executed sequentially if FORCE_SEQUENTIAL for unrolled method"() {
    def listener = Mock(RunListener)
    def jUnitCore = new JUnitCore()
    jUnitCore.addListener(listener)

    def clazz = SampleTestWithSequentialOnMethodLevel.class
    when:
    def runner = new EmbeddedSpecRunner()
    runner.withNewContext {
      jUnitCore.run(ParallelComputer.methods(), clazz);
    }
    then:
    1 * listener.testRunStarted(_)
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=1"))
    1 * listener.testStarted(testDescription(clazz, "test simple"))
    1 * listener.testStarted(testDescription(clazz, "test no unroll"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=1"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testStarted(testDescription(clazz, "test for x=2"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testFinished(testDescription(clazz, "test for x=2"))
    1 * listener.testFinished(testDescription(clazz, "test simple"))
    1 * listener.testFinished(testDescription(clazz, "test no unroll"))
    0 * listener.testFailure(_)
    then:
    1 * listener.testRunFinished(_)
  }

  private static Description testDescription(Class clazz, String testName) {
    Description.createTestDescription(clazz, testName)
  }

}

class SampleTest extends Specification {

  @Unroll
  def "test for x=#x"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }

  def "test simple"() {
    def x = 1;
    when:
    sleep 1000
    then:
    x == x
  }

  def "test no unroll"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }
}

class SampleTestWithTimeout extends Specification {

  def setup() {
    sleep 1000
  }

  @Unroll
  def "test for x=#x"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }

  def "test simple"() {
    def x = 1;
    when:
    sleep 1000
    then:
    x == x
  }

  def "test no unroll"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }
}

@ConcurrentExecutionMode(ConcurrentExecutionMode.Mode.FORCE_SEQUENTIAL)
class SampleTestWithSequentialOnClassLevel extends Specification {

  @Unroll
  def "test for x=#x"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }

  def "test simple"() {
    def x = 1;
    when:
    sleep 1000
    then:
    x == x
  }

  def "test no unroll"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }
}

class SampleTestWithSequentialOnMethodLevel extends Specification {

  @ConcurrentExecutionMode(ConcurrentExecutionMode.Mode.FORCE_SEQUENTIAL)
  @Unroll
  def "test for x=#x"() {
    when:
    sleep 300
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }

  def "test simple"() {
    def x = 1;
    when:
    sleep 1000
    then:
    x == x
  }

  def "test no unroll"() {
    when:
    sleep 1000
    then:
    x == x
    where:
    x | _
    1 | _
    2 | _
  }
}
