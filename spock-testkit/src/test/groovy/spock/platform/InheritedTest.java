package spock.platform;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import spock.testkit.testsources.InheritedChildTestCase;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.testkit.engine.EventStatistics;

class InheritedTest extends SpockEngineBase {

  @Test
  void verifyInheritedTests() {
    Consumer<EventStatistics> assertions = stats -> stats.started(3).succeeded(2).failed(1).skipped(1);
    execute(selectClass(InheritedChildTestCase.class), assertions, Arrays.asList(new TestMethodFilter(Pattern.compile(".*InheritedChildTestCase"))));
  }

  /**
   * Copy relevant features of Maven Surefire Plugin's TestMethodFilter to recreate it here
   */
  class TestMethodFilter
    implements PostDiscoveryFilter {

    private final Pattern classPattern;

    TestMethodFilter(Pattern classPattern) {
      this.classPattern = classPattern;
    }

    @Override
    public FilterResult apply(TestDescriptor descriptor) {
      boolean shouldRun = descriptor.getSource()
        .filter(MethodSource.class::isInstance)
        .map(MethodSource.class::cast)
        .map(this::shouldRun)
        .orElse(true);

      return FilterResult.includedIf(shouldRun);
    }

    private boolean shouldRun(MethodSource source) {
      String testClass = source.getClassName();
      return this.classPattern.matcher(testClass).matches();
    }
  }

}
