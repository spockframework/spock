package org.spockframework.runtime;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.ExceptionUtil;
import spock.config.RunnerConfiguration;

import org.junit.platform.engine.*;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A non-parametric feature (test) that only has a single "iteration".
 * Contrary to parametric tests - where the iterations are the children - this is the actual test. The execution
 * is only delegated, but does not cause any extra test events for the single iteration.
 * <p>
 * All node events are also delegated to the {@link IterationNode} in the correct order.
 */
public class SimpleFeatureNode extends FeatureNode {

  private final IterationNode delegate;

  public SimpleFeatureNode(UniqueId uniqueId, RunnerConfiguration configuration,
                           FeatureInfo featureInfo, IterationNode delegate) {
    super(uniqueId, featureInfo.getDisplayName(), featureToMethodSource(featureInfo), configuration, featureInfo);
    this.delegate = delegate;
  }

  @Override
  public Type getType() {
    return delegate.getType();
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    context = super.prepare(context);
    return context.withCurrentFeature(getNodeInfo());
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(getNodeInfo());

    AtomicReference<Throwable> result = new AtomicReference<>();
    EngineExecutionListener executionListener = new EngineExecutionListener() {
      @Override
      public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        result.set(new TestAbortedException(reason));
      }

      @Override
      public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        testExecutionResult.getThrowable().ifPresent(result::set);
      }
    };

    addChild(delegate);
    dynamicTestExecutor.execute(delegate, executionListener);
    dynamicTestExecutor.awaitFinished();

    if (result.get() != null) {
      ExceptionUtil.sneakyThrow(result.get());
    }
    return context;
  }
}
