package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.DataIteratorFactory;
import org.spockframework.runtime.IDataIterator;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;
import spock.lang.RepeatUntilFailure;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.spockframework.runtime.DataIteratorFactory.UNKNOWN_ITERATIONS;

public class RepeatUntilFailureExtension implements IAnnotationDrivenExtension<RepeatUntilFailure> {
  @Override
  public void visitFeatureAnnotation(RepeatUntilFailure annotation, FeatureInfo feature) {
    if (annotation.ignoreRest()) {
      feature.getSpec().getBottomSpec().getAllFeatures()
        .stream()
        .filter(f -> (f != feature) && (f.getFeatureMethod().getAnnotation(RepeatUntilFailure.class) == null))
        .forEach(f -> f.skip("Focussed test run with @RepeatUntilFailure."));
    }

    feature.setForceParameterized(true);
    feature.setDataDriver(new RepeatUntilFailureDataDriver(annotation.maxAttempts()));
  }

  private static class RepeatUntilFailureDataDriver implements IDataDriver {
    private final int maxAttempts;

    public RepeatUntilFailureDataDriver(int maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    @Override
    public void runIterations(IDataIterator dataIterator, IIterationRunner iterationRunner, List<ParameterInfo> parameters) {
      int estimatedNumIterations = dataIterator.getEstimatedNumIterations();
      int maxIterations = estimatedNumIterations == UNKNOWN_ITERATIONS ? UNKNOWN_ITERATIONS : (estimatedNumIterations * maxAttempts);
      List<Object[]> arguments = estimatedNumIterations == UNKNOWN_ITERATIONS ? new ArrayList<>() : new ArrayList<>(estimatedNumIterations);
      dataIterator.forEachRemaining(arguments::add);
      for (int attempt = 0; attempt < maxAttempts; attempt++) {
        for (Object[] args : arguments) {
          try {
            ExecutionResult executionResult = iterationRunner.runIteration(args, maxIterations).get();
            if (executionResult == ExecutionResult.FAILED) {
              return;
            }
          } catch (InterruptedException | ExecutionException e) {
            return;
          }
        }
      }
    }
  }
}
