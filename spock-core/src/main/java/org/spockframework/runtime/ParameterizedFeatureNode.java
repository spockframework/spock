package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;
import spock.config.RunnerConfiguration;

import java.util.*;

import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.UniqueId;
import org.opentest4j.*;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.TestExecutionResult.Status.*;
import static org.spockframework.util.ExceptionUtil.sneakyThrow;

public class ParameterizedFeatureNode extends FeatureNode {

  protected ParameterizedFeatureNode(UniqueId uniqueId, RunnerConfiguration configuration, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getDisplayName(), featureToMethodSource(featureInfo), configuration, featureInfo);
  }


  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    if (getNodeInfo().isSkipped()) {
      // Node.prepare is called before Node.shouldBeSkipped so we just skip the prepare
      return context;
    }
    getNodeInfo().setIterationNameProvider(new SafeIterationNameProvider(getNodeInfo().getIterationNameProvider()));
    return context.withCurrentFeature(getNodeInfo()).withParentId(getUniqueId());

  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(getNodeInfo());
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    ParameterizedFeatureChildExecutor childExecutor = new ParameterizedFeatureChildExecutor(this, dynamicTestExecutor);
    context.getRunner().runParameterizedFeature(context, childExecutor);
    errorInfoCollector.assertEmpty();
    if (childExecutor.getExecutionCount() < 1) {
     throw new SpockExecutionException("Data provider has no data");
    }

    // do not try to aggregate iteration results if they are reported individually
    if (getNodeInfo().isReportIterations()) {
      return context;
    }

    Map<Status, Queue<Throwable>> childResults = childExecutor.getResults();
    if (childResults.containsKey(FAILED)) {
      handleFailedChildren(childResults);
    } else if (!childResults.containsKey(SUCCESSFUL)) {
      handleOnlyAbortedChildren(childResults);
    }

    return context;
  }

  private void handleFailedChildren(Map<Status, Queue<Throwable>> childResults) {
    if (!childResults.containsKey(FAILED)) {
      throw new AssertionError("This method should only be called if there are aborted children and nothing else");
    }

    List<Throwable> failures = childResults
      .get(FAILED)
      .stream()
      .filter(Objects::nonNull)
      .collect(toList());

    switch (failures.size()) {
      case 0:
        throw new SpockAssertionError("At least one iteration failed");

      case 1:
        sneakyThrow(failures.get(0));

      default:
        MultipleFailuresError multipleFailuresError = new MultipleFailuresError(null, failures);
        multipleFailuresError.setStackTrace(failures.get(0).getStackTrace());
        throw multipleFailuresError;
    }
  }

  private void handleOnlyAbortedChildren(Map<Status, Queue<Throwable>> childResults) {
    if ((childResults.size() != 1) || !childResults.containsKey(ABORTED)) {
      throw new AssertionError("This method should only be called if there are aborted children and nothing else");
    }

    List<Throwable> abortions = childResults
      .get(ABORTED)
      .stream()
      .filter(Objects::nonNull)
      .collect(toList());

    Throwable abortion;
    switch (abortions.size()) {
      case 0:
        abortion = new TestAbortedException("All iterations were aborted");
        break;

      case 1:
        abortion = abortions.get(0);
        break;

      default:
        abortion = new MultipleFailuresError(null, abortions);
        abortion.setStackTrace(abortions.get(0).getStackTrace());
        break;
    }

    TestAbortedException testAbortedException;
    if (abortion instanceof TestAbortedException) {
      testAbortedException = (TestAbortedException) abortion;
    } else {
      testAbortedException = new TestAbortedException("All iterations were aborted", abortion);
      testAbortedException.setStackTrace(abortion.getStackTrace());
    }
    throw testAbortedException;
  }

  @Override
  public Type getType() {
    return getNodeInfo().isReportIterations() ? Type.CONTAINER_AND_TEST : Type.TEST;
  }
}
