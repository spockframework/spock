package org.spockframework.runtime;

import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;
import org.spockframework.util.ExceptionUtil;

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
    List<Throwable> failures = context.getFailures();
    if (!failures.isEmpty()) {
      Throwable failure;
      if (failures.size() == 1) {
        failure = failures.get(0);
      } else {
        failure = new MultipleFailuresError(null, failures);
      }

      if (context.getCurrentFeature().isReportIterations()) {
        throw new SpockAssertionError("At least one iteration failed", failure);
      } else {
        ExceptionUtil.sneakyThrow(failure);
      }
    } else if (!context.hadSuccess()) {
      List<Throwable> abortions = context.getAbortions();
      Throwable abortion;
      if (abortions.size() == 1) {
        abortion = abortions.get(0);
      } else {
        abortion = new MultipleFailuresError(null, abortions);
      }

      throw new TestAbortedException("All iterations were aborted", abortion);
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
