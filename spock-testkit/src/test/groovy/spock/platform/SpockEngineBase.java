package spock.platform;

import java.util.function.Consumer;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.testkit.engine.*;

public class SpockEngineBase {
  protected void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer) {
    execute(selector)
      .testEvents()
      .debug()
      .assertStatistics(statisticsConsumer);
  }

  protected EngineExecutionResults execute(DiscoverySelector selector) {
    return EngineTestKit
      .engine("spock")
      .selectors(selector)
      .execute();
  }
}
