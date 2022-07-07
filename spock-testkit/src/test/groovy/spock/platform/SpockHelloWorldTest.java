package spock.platform;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.spockframework.runtime.SpockEngine;
import spock.testkit.testsources.*;

import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.*;
import org.junit.platform.testkit.engine.*;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

class SpockHelloWorldTest extends SpockEngineBase {

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
  void mixedDiscoveryOfClassAndUniqueIdSelectorsIsSupportedRegardlessOfOrder() {
    UniqueId classUniqueId = UniqueId.forEngine("spock").append("spec", StepwiseTestCase.class.getName());

    Consumer<EventStatistics> assertions = stats -> stats.started(4).succeeded(3).failed(1).skipped(1);
    execute(asList(
      selectClass(StepwiseTestCase.class),
      selectUniqueId(classUniqueId.append("feature", "$spock_feature_0_0"))
      ), assertions);
    execute(asList(
      selectUniqueId(classUniqueId.append("feature", "$spock_feature_0_0")),
      selectClass(StepwiseTestCase.class)
      ), assertions);
  }

  @Test
  void packageSelectorsAreResolved() {
    assertEquals(11, execute(selectPackage(ExampleTestCase.class.getPackage().getName()))
      .containerEvents()
      .filter(event -> event.getType() == EventType.STARTED)
      .filter(event -> "spec".equals(event.getTestDescriptor().getUniqueId().getLastSegment().getType()))
      .count());
  }

  @Test
  void iterationsAreResolved() {
    UniqueId featureMethodUniqueId = UniqueId.forEngine("spock")
      .append("spec", UnrollTestCase.class.getName())
      .append("feature", "$spock_feature_0_0");

    execute(
      selectUniqueId(featureMethodUniqueId.append("iteration", "1")),
      stats -> stats.started(2).succeeded(1).failed(1)
    );
    execute(
      asList(
        selectUniqueId(featureMethodUniqueId.append("iteration", "0")),
        selectUniqueId(featureMethodUniqueId.append("iteration", "2"))
      ),
      stats -> stats.started(3).succeeded(3)
    );
    execute(
      asList(
        selectUniqueId(featureMethodUniqueId.append("iteration", "0")),
        selectUniqueId(featureMethodUniqueId)
      ),
      stats -> stats.started(4).succeeded(3).failed(1)
    );
  }

  @Test
  void verifyUnrollExample() {
    execute(selectClass(UnrollTestCase.class), stats -> stats.started(13).succeeded(12).failed(1));
  }

  @Test
  void verifyStepwiseExample() {
    execute(selectClass(StepwiseTestCase.class), stats -> stats.started(4).succeeded(3).failed(1).skipped(1));
  }

  @Test
  void verifyErrorExample() {
    Launcher launcher = LauncherFactory.create(LauncherConfig.builder()
      .enableTestEngineAutoRegistration(false)
      .enableTestExecutionListenerAutoRegistration(false)
      .addTestEngines(new SpockEngine())
      .build());
    TestPlan testPlan = launcher.discover(LauncherDiscoveryRequestBuilder.request()
      .selectors(selectClass(ErrorTestCase.class))
      .build());
    assertEquals(1, testPlan.getChildren(testPlan.getRoots().iterator().next()).size());

    execute(selectClass(StepwiseTestCase.class), stats -> stats.started(4).succeeded(3).failed(1).skipped(1));
  }

  @ParameterizedTest
  @ValueSource(classes = {SharedSetupCleanupTestCase.class, SetupCleanupTestCase.class, CleanupTestCase.class})
  void verifySingleExample(Class<?> testClass) {
    Consumer<EventStatistics> assertions = stats -> stats.started(1).succeeded(1);
    execute(selectClass(testClass), assertions);
    execute(selectUniqueId("[engine:spock]/[spec:" + testClass.getName() + "]"), assertions);
  }
}
