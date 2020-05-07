package org.spockframework.runtime;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor;
import org.opentest4j.TestAbortedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;

class ParameterizedFeatureChildExecutor {

  private final ParameterizedFeatureNode parameterizedFeatureNode;
  private AtomicInteger executionCounter = new AtomicInteger();

  private Map<TestExecutionResult.Status, Queue<Throwable>> results = new ConcurrentHashMap<>();

  private final DynamicTestExecutor delegate;
  private EngineExecutionListener executionListener;

  ParameterizedFeatureChildExecutor(ParameterizedFeatureNode parameterizedFeatureNode, DynamicTestExecutor delegate) {
    this.parameterizedFeatureNode = parameterizedFeatureNode;
    this.delegate = delegate;

    // if we are not going to report single iterations,
    // we will need a listener for the child executions
    // so create it here once and use it repeatedly later on
    if (!parameterizedFeatureNode.featureInfo.isReportIterations()) {
      executionListener = new EngineExecutionListener() {
        @Override
        public void executionSkipped(TestDescriptor testDescriptor, String reason) {
          results
            .computeIfAbsent(ABORTED, status -> new ConcurrentLinkedQueue<>())
            .add(new TestAbortedException(reason));
        }

        @Override
        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
          Queue<Throwable> failures = results
            .computeIfAbsent(testExecutionResult.getStatus(), status -> new ConcurrentLinkedQueue<>());
          testExecutionResult.getThrowable().ifPresent(failures::add);
        }
      };
    }
  }

  public void execute(TestDescriptor testDescriptor) {
    executionCounter.incrementAndGet();
    parameterizedFeatureNode.addChild(testDescriptor);
    if (parameterizedFeatureNode.featureInfo.isReportIterations()) {
      delegate.execute(testDescriptor);
    } else {
      delegate.execute(testDescriptor, executionListener);
    }
  }

  public void awaitFinished() throws InterruptedException {
    delegate.awaitFinished();
  }

  int getExecutionCount() {
    return executionCounter.get();
  }

  Map<TestExecutionResult.Status, Queue<Throwable>> getResults() {
    return results;
  }
}
