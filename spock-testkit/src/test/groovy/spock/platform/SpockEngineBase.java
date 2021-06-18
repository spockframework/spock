package spock.platform;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventStatistics;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SpockEngineBase {
  protected void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer) {
    execute(selector, statisticsConsumer, Collections.emptyList());
  }
  protected void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer, List<Filter> filters) {
    execute(selector, filters)
      .testEvents()
      .debug()
      .assertStatistics(statisticsConsumer);
  }

  protected EngineExecutionResults execute(DiscoverySelector selector) {
    return execute(selector, Collections.emptyList());
  }

  protected EngineExecutionResults execute(DiscoverySelector selector, List<Filter> filters) {
    return EngineTestKit
      .engine("spock")
      .selectors(selector)
      .filters(filters.toArray(Filter[]::new))
      .execute();
  }
}
