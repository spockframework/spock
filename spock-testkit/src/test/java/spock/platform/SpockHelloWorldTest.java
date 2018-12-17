package spock.platform;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import spock.testkit.testsources.ExampleTestCase;

public class SpockHelloWorldTest {
  @Test
  void verifyJupiterContainerStats() {
    EngineTestKit
      .engine("spock")
      .selectors(selectClass(ExampleTestCase.class))
      .execute()
      .tests()
      .debug()
      .assertStatistics(stats -> stats.started(2).succeeded(1).failed(1).skipped(1));
  }
}
