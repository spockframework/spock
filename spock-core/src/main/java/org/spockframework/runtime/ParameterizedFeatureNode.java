package org.spockframework.runtime;

import org.junit.platform.engine.TestExecutionResult.Status;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;

import static java.util.stream.Collectors.*;
import static org.junit.platform.engine.TestExecutionResult.Status.*;
import static org.spockframework.util.ExceptionUtil.sneakyThrow;

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

    // do not try to aggregate iteration results if they are reported individually
    if (featureInfo.isReportIterations()) {
      return context;
    }

    if (testExecutor.results.containsKey(FAILED)) {
      List<Throwable> failures = testExecutor.results
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
    } else if (!testExecutor.results.containsKey(SUCCESSFUL)) {
      List<Throwable> abortions = testExecutor.results
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

    return context;
  }

  @Override
  public Type getType() {
    return featureInfo.isReportIterations() ? Type.CONTAINER_AND_TEST : Type.TEST;
  }

  class ChildExecutor {

    private AtomicInteger executionCounter = new AtomicInteger();

    private Map<Status, List<Throwable>> results = new HashMap<>();

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    public void execute(TestDescriptor testDescriptor) {
      executionCounter.incrementAndGet();
      addChild(testDescriptor);
      if (featureInfo.isReportIterations()) {
        delegate.execute(testDescriptor);
      } else {
        delegate.execute(testDescriptor, new EngineExecutionListener() {
          @Override
          public void executionSkipped(TestDescriptor testDescriptor, String reason) {
            results
              .computeIfAbsent(ABORTED, status -> new ArrayList<>())
              .add(new TestAbortedException(reason));
          }

          @Override
          public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
            List<Throwable> failures = results
              .computeIfAbsent(testExecutionResult.getStatus(), status -> new ArrayList<>());
            testExecutionResult.getThrowable().ifPresent(failures::add);
          }
        });
      }
    }

    public void awaitFinished() throws InterruptedException {
      delegate.awaitFinished();
    }

    int getExecutionCount() {
      return executionCounter.get();
    }
  }
}
