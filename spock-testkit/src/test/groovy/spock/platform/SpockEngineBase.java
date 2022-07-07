package spock.platform;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventStatistics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SpockEngineBase {

  private static final Filter<?>[] FILTERS = new Filter<?>[0];
  private static final DiscoverySelector[] SELECTORS = new DiscoverySelector[0];

  protected void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer) {
    execute(selector, statisticsConsumer, emptyList());
  }
  protected void execute(List<DiscoverySelector> selector, Consumer<EventStatistics> statisticsConsumer) {
    execute(selector, statisticsConsumer, emptyList());
  }
  protected void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer, List<Filter<?>> filters) {
    execute(singletonList(selector), statisticsConsumer, filters);
  }
  protected void execute(List<DiscoverySelector> selector, Consumer<EventStatistics> statisticsConsumer, List<Filter<?>> filters) {
    execute(selector, filters)
      .testEvents()
      .debug()
      .assertStatistics(statisticsConsumer);
  }

  protected EngineExecutionResults execute(DiscoverySelector... selector) {
    return execute(asList(selector), emptyList());
  }

  protected EngineExecutionResults execute(List<DiscoverySelector> selectors, List<Filter<?>> filters) {
    return EngineTestKit
      .engine("spock")
      .selectors(selectors.toArray(SELECTORS))
      .filters(filters.toArray(FILTERS))
      .execute();
  }
}
