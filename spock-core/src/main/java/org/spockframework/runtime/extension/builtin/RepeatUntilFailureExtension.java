package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.IDataIterator;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IDataDriver;
import org.spockframework.runtime.extension.IIterationRunner;
import org.spockframework.runtime.model.ExecutionResult;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.ParameterInfo;
import spock.lang.RepeatUntilFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RepeatUntilFailureExtension implements IAnnotationDrivenExtension<RepeatUntilFailure> {
  @Override
  public void visitFeatureAnnotation(RepeatUntilFailure annotation, FeatureInfo feature) {
    if (annotation.ignoreRest()) {
      feature.getSpec().getBottomSpec().getAllFeatures()
        .stream()
        .filter(f -> f != feature)
        .forEach(f -> f.skip("Focussed test run with @RepeatUntilFailure for a single feature."));
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
      List<Object[]> data = new ArrayList<>(dataIterator.getEstimatedNumIterations());
      dataIterator.forEachRemaining(data::add);
      for (int attempt = 0; attempt < maxAttempts; attempt++) {
        for (Object[] args : data) {
          try {
            ExecutionResult executionResult = iterationRunner.runIteration(args).get();
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
