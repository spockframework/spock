package org.spockframework.runtime;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor;
import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.ExecutionResult;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;

class ParameterizedFeatureChildExecutor {

  private final ParameterizedFeatureNode parameterizedFeatureNode;
  private final AtomicInteger executionCounter = new AtomicInteger();

  private final Map<TestExecutionResult.Status, Queue<Throwable>> results = new ConcurrentHashMap<>();
  private final Map<TestDescriptor, CompletableFuture<ExecutionResult>> pending = new ConcurrentHashMap<>();

  private final DynamicTestExecutor delegate;
  private final EngineExecutionListener executionListener;

  ParameterizedFeatureChildExecutor(ParameterizedFeatureNode parameterizedFeatureNode,
                                    DynamicTestExecutor delegate,
                                    EngineExecutionListener delegateEngineExecutionListener) {
    this.parameterizedFeatureNode = parameterizedFeatureNode;
    this.delegate = delegate;

    if (parameterizedFeatureNode.getNodeInfo().isReportIterations()) {
      executionListener = new EngineExecutionListener() {
        @Override
        public void dynamicTestRegistered(TestDescriptor testDescriptor) {
          delegateEngineExecutionListener.dynamicTestRegistered(testDescriptor);
        }

        @Override
        public void executionSkipped(TestDescriptor testDescriptor, String reason) {
          delegateEngineExecutionListener.executionSkipped(testDescriptor, reason);
          pending.remove(testDescriptor).complete(ExecutionResult.ABORTED);
        }

        @Override
        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
          delegateEngineExecutionListener.executionFinished(testDescriptor, testExecutionResult);
          ExecutionResult result = testExecutionResult.getStatus() == SUCCESSFUL ?
            ExecutionResult.SUCCESSFUL : ExecutionResult.FAILED;
          pending.remove(testDescriptor).complete(result);
        }

        @Override
        public void executionStarted(TestDescriptor testDescriptor) {
          delegateEngineExecutionListener.executionStarted(testDescriptor);
        }

        @Override
        public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
          delegateEngineExecutionListener.reportingEntryPublished(testDescriptor, entry);
        }
      };
    } else {
      // if we are not going to report single iterations,
      // we will need a listener for the child executions
      executionListener = new EngineExecutionListener() {
        @Override
        public void executionSkipped(TestDescriptor testDescriptor, String reason) {
          results
            .computeIfAbsent(ABORTED, status -> new ConcurrentLinkedQueue<>())
            .add(new TestAbortedException(reason));
          pending.remove(testDescriptor).complete(ExecutionResult.ABORTED);
        }

        @Override
        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
          Queue<Throwable> failures = results
            .computeIfAbsent(testExecutionResult.getStatus(), status -> new ConcurrentLinkedQueue<>());
          testExecutionResult.getThrowable().ifPresent(failures::add);
          ExecutionResult result = testExecutionResult.getStatus() == SUCCESSFUL ?
            ExecutionResult.SUCCESSFUL : ExecutionResult.FAILED;
          pending.remove(testDescriptor).complete(result);
        }
      };
    }
  }

  public CompletableFuture<ExecutionResult> execute(TestDescriptor testDescriptor) {
    executionCounter.incrementAndGet();
    parameterizedFeatureNode.addChild(testDescriptor);
    CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
    pending.put(testDescriptor, future);
    delegate.execute(testDescriptor, executionListener);
    return future;
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
