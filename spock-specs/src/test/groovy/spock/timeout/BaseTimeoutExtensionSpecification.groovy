package spock.timeout

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.AutoCleanup
import spock.lang.ResourceLock

import java.util.concurrent.TimeUnit

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
abstract class BaseTimeoutExtensionSpecification extends EmbeddedSpecification {

  @AutoCleanup("stop")
  StandardStreamsCapturer outputCapturer = new StandardStreamsCapturer()
  OutputListener outputListener = new OutputListener()

  def setup() {
    runner.addClassMemberImport TimeUnit
    outputCapturer.addStandardStreamsListener(outputListener)
    outputCapturer.start()
  }

  protected void runSpecWithInterrupts(int interruptAttempts) {
    runner.runSpecBody """
      @Timeout(value = 100, unit = MILLISECONDS)
      def foo() {
        ${(1..interruptAttempts).collect {
      """
        when: Thread.sleep 99999999999
        then: thrown InterruptedException
      """
    }.join()}
      }
    """
  }

  protected void assertThreadDumpsCaptured(int unsuccessfulInterruptAttempts, int threadDumps, boolean exceededCaptureLimit, ThreadDumpUtilityType util = ThreadDumpUtilityType.JCMD) {
    with(outputListener) {
      count("Method 'foo' has not stopped") == unsuccessfulInterruptAttempts
      count("Thread dump of current JVM (${util.name()})") == threadDumps
      // just some thread that is always there
      count(/"Signal Dispatcher" #/) == threadDumps
      (count('No further thread dumps will be logged and no timeout listeners will be run') == 1) == exceededCaptureLimit
    }
  }

  private class OutputListener implements IStandardStreamsListener {

    private final StringBuilder messages = new StringBuilder()

    @Override
    void standardOut(String message) {
      messages.append(message)
    }

    @Override
    void standardErr(String message) {
      messages.append(message)
    }

    private List<String> getLines() {
      messages.toString().readLines()
    }

    int count(String sequence) {
      lines.count { it.contains(sequence) }
    }
  }
}
