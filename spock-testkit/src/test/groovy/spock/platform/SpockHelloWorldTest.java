package spock.platform;

import spock.testkit.testsources.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class SpockHelloWorldTest {
  @Test
  void verifySimpleExample() {
    EngineTestKit
      .engine("spock")
      .selectors(selectClass(ExampleTestCase.class))
      .execute()
      .tests()
      .debug()
      .assertStatistics(stats -> stats.started(2).succeeded(1).failed(1).skipped(1));
  }

  @Test
  void verifyUnrollExample() {
    EngineTestKit
      .engine("spock")
      .selectors(selectClass(UnrollTestCase.class))
      .execute()
      .tests()
      .debug()
      .assertStatistics(stats -> stats.started(10).succeeded(10));
  }

  @Test
  void verifyStepwiseExample() {
    EngineTestKit
      .engine("spock")
      .selectors(selectClass(StepwiseTestCase.class))
      .execute()
      .tests()
      .debug()
      .assertStatistics(stats -> stats.started(4).succeeded(3).failed(1).skipped(1));
  }

  @ParameterizedTest
  @ValueSource(classes = {SharedSetupCleanupTestCase.class, SetupCleanupTestCase.class, CleanupTestCase.class})
  void verifySingleExample(Class<?> testClass) {
    EngineTestKit
      .engine("spock")
      .selectors(selectClass(testClass))
      .execute()
      .tests()
      .debug()
      .assertStatistics(stats -> stats.started(1).succeeded(1));
  }
}
