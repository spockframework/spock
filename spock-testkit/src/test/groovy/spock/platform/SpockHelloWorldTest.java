package spock.platform;

import spock.testkit.testsources.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.*;
import org.junit.platform.testkit.engine.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

class SpockHelloWorldTest {

  @Test
  void verifySimpleExample() {
    Consumer<EventStatistics> assertions = stats -> stats.started(2).succeeded(1).failed(1).skipped(1);
    execute(selectClass(ExampleTestCase.class), assertions);
    UniqueId classUniqueId = UniqueId.forEngine("spock").append("spec", ExampleTestCase.class.getName());
    execute(selectUniqueId(classUniqueId), assertions);

    assertions = stats -> stats.started(1).succeeded(1).failed(0).skipped(0);
    execute(selectUniqueId(classUniqueId.append("feature", "$spock_feature_0_0")), assertions);
    execute(selectMethod(ExampleTestCase.class, "$spock_feature_0_0"), assertions);
    execute(selectMethod(ExampleTestCase.class, "first"), assertions);

    assertions = stats -> stats.started(1).succeeded(0).failed(1).skipped(0);
    execute(selectUniqueId(classUniqueId.append("feature", "$spock_feature_0_1")), assertions);
    execute(selectMethod(ExampleTestCase.class, "$spock_feature_0_1"), assertions);
    execute(selectMethod(ExampleTestCase.class, "failMe"), assertions);

    assertions = stats -> stats.started(0).succeeded(0).failed(0).skipped(1);
    execute(selectUniqueId(classUniqueId.append("feature", "$spock_feature_0_2")), assertions);
    execute(selectMethod(ExampleTestCase.class, "$spock_feature_0_2"), assertions);
    execute(selectMethod(ExampleTestCase.class, "ignoreMe"), assertions);
  }

  @Test
  void packageSelectorsAreResolved() {
    assertEquals(6, execute(selectPackage(ExampleTestCase.class.getPackage().getName()))
      .containers()
      .filter(event -> event.getType() == EventType.STARTED)
      .filter(event -> "spec".equals(event.getTestDescriptor().getUniqueId().getLastSegment().getType()))
      .count());
  }

  @Test
  void verifyUnrollExample() {
    execute(selectClass(UnrollTestCase.class), stats -> stats.started(16).succeeded(16));
  }

  @Test
  void verifyStepwiseExample() {
    execute(selectClass(StepwiseTestCase.class), stats -> stats.started(4).succeeded(3).failed(1).skipped(1));
  }

  @ParameterizedTest
  @ValueSource(classes = {SharedSetupCleanupTestCase.class, SetupCleanupTestCase.class, CleanupTestCase.class})
  void verifySingleExample(Class<?> testClass) {
    Consumer<EventStatistics> assertions = stats -> stats.started(1).succeeded(1);
    execute(selectClass(testClass), assertions);
    execute(selectUniqueId("[engine:spock]/[spec:" + testClass.getName() + "]"), assertions);
  }

  private void execute(DiscoverySelector selector, Consumer<EventStatistics> statisticsConsumer) {
    execute(selector)
      .tests()
      .debug()
      .assertStatistics(statisticsConsumer);
  }

  private EngineExecutionResults execute(DiscoverySelector selector) {
    return EngineTestKit
      .engine("spock")
      .selectors(selector)
      .execute();
  }

}
