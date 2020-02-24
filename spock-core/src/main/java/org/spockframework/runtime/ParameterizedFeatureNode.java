package org.spockframework.runtime;

import org.junit.platform.engine.TestExecutionResult.Status;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;
import org.spockframework.util.ExceptionUtil;

import static java.util.stream.Collectors.*;
import static org.junit.platform.engine.TestExecutionResult.Status.*;

public class ParameterizedFeatureNode extends FeatureNode {

  protected ParameterizedFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(),featureToMethodSource(featureInfo), featureInfo);
  }


  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    featureInfo.setIterationNameProvider(new SafeIterationNameProvider(featureInfo.getIterationNameProvider()));
    return context.withCurrentFeature(featureInfo).withParentId(getUniqueId());
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(featureInfo);
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    ChildExecutor testExecutor = new ChildExecutor(dynamicTestExecutor);
    context.getRunner().runParameterizedFeature(context, testExecutor);
    errorInfoCollector.assertEmpty();
    if (testExecutor.getExecutionCount() < 1) {
     throw new SpockExecutionException("Data provider has no data");
    }
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    Map<Status, List<Throwable>> iterationResults = children
      .stream()
      .map(IterationNode.class::cast)
      .map(IterationNode::getResult)
      .collect(groupingBy(
        TestExecutionResult::getStatus,
        mapping(
          testExecutionResult -> testExecutionResult
            .getThrowable()
            .orElse(null),
          toList())));

    if (iterationResults.containsKey(FAILED)) {
      List<Throwable> failures = iterationResults
        .get(FAILED)
        .stream()
        .filter(Objects::nonNull)
        .collect(toList());

      Throwable failure;
      switch (failures.size()) {
        case 0:
          failure = new SpockAssertionError("At least one iteration failed");
          break;

        case 1:
          failure = failures.get(0);
          break;

        default:
          failure = new MultipleFailuresError(null, failures);
          failure.setStackTrace(failures.get(0).getStackTrace());
          break;
      }

      if (featureInfo.isReportIterations()) {
        SpockAssertionError exception = new SpockAssertionError("At least one iteration failed", failure);
        exception.setStackTrace(failure.getStackTrace());
        throw exception;
      } else {
        ExceptionUtil.sneakyThrow(failure);
      }
    } else if (!iterationResults.containsKey(SUCCESSFUL)) {
      List<Throwable> abortions = iterationResults
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

      if (featureInfo.isReportIterations() || !(abortion instanceof TestAbortedException)) {
        TestAbortedException exception = new TestAbortedException("All iterations were aborted", abortion);
        exception.setStackTrace(abortion.getStackTrace());
        throw exception;
      } else {
        ExceptionUtil.sneakyThrow(abortion);
      }
    }
  }

  @Override
  public Type getType() {
    return featureInfo.isReportIterations() ? Type.CONTAINER_AND_TEST : Type.TEST;
  }

  class ChildExecutor implements DynamicTestExecutor {

    private AtomicInteger executionCounter = new AtomicInteger();

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    @Override
    public void execute(TestDescriptor testDescriptor) {
      executionCounter.incrementAndGet();
      addChild(testDescriptor);
      delegate.execute(testDescriptor);
    }

    @Override
    public void awaitFinished() throws InterruptedException {
      delegate.awaitFinished();
    }

    int getExecutionCount() {
      return executionCounter.get();
    }
  }
}
