package org.spockframework.runtime;

import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.reporting.ReportEntry;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.model.FeatureInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;

import static java.lang.reflect.Modifier.FINAL;
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

  class ChildExecutor implements DynamicTestExecutor {

    private AtomicInteger executionCounter = new AtomicInteger();

    private Map<Status, List<Throwable>> results = new HashMap<>();

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    @Override
    public void execute(TestDescriptor testDescriptor) {
      executionCounter.incrementAndGet();
      addChild(testDescriptor);
      if (featureInfo.isReportIterations()) {
        delegate.execute(testDescriptor);
      } else {
        //TODO: replace by result of https://github.com/junit-team/junit5/issues/2188
        executeReplacingListener(delegate, testDescriptor, new EngineExecutionListener() {
          @Override
          public void dynamicTestRegistered(TestDescriptor testDescriptor) {
          }

          @Override
          public void executionSkipped(TestDescriptor testDescriptor, String reason) {
            results
              .computeIfAbsent(ABORTED, status -> new ArrayList<>())
              .add(new TestAbortedException(reason));
          }

          @Override
          public void executionStarted(TestDescriptor testDescriptor) {
          }

          @Override
          public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
            List<Throwable> failures = results
              .computeIfAbsent(testExecutionResult.getStatus(), status -> new ArrayList<>());
            testExecutionResult.getThrowable().ifPresent(failures::add);
          }

          @Override
          public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
          }
        });
      }
    }

    //TODO: work-around for missing https://github.com/junit-team/junit5/issues/2188
    private void executeReplacingListener(DynamicTestExecutor delegate, TestDescriptor testDescriptor,
                                          EngineExecutionListener listener) {
      try {
        // get the enclosing NodeTestTask instance of the DynamicTestExecutor
        Field nodeTestTaskField = delegate.getClass().getDeclaredField("this$0");
        nodeTestTaskField.setAccessible(true);
        Object nodeTestTask = nodeTestTaskField.get(delegate);

        // get the NodeTestTaskContext field value of the NodeTestTask
        Field taskContextField = nodeTestTask.getClass().getDeclaredField("taskContext");
        taskContextField.setAccessible(true);
        Object taskContext = taskContextField.get(nodeTestTask);

        // get the EngineExecutionListener field of the NodeTestTaskContext
        Field listenerField = taskContext.getClass().getDeclaredField("listener");
        listenerField.setAccessible(true);

        // remove the final modifier from the EngineExecutionListener field
        Method getDeclaredFields = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields.setAccessible(true);
        Field modifiersField = Arrays
          .stream((Field[]) getDeclaredFields.invoke(Field.class, false))
          .filter(field -> field.getName().equals("modifiers"))
          .findAny()
          .orElseThrow(AssertionError::new);
        modifiersField.setAccessible(true);
        modifiersField.setInt(listenerField, listenerField.getModifiers() & ~FINAL);

        // store the original listener
        Object originalListener = listenerField.get(taskContext);
        try {
          // set the custom listener
          listenerField.set(taskContext, listener);
          // execute the iterations
          delegate.execute(testDescriptor);
        } finally {
          // restore the original listener
          listenerField.set(taskContext, originalListener);
        }
      } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
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
