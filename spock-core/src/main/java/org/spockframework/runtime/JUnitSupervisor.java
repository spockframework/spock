/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.junit.ComparisonFailure;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import org.junit.runners.model.MultipleFailureException;
import org.spockframework.runtime.condition.IObjectRenderer;
import org.spockframework.runtime.model.*;
import org.spockframework.util.TextUtil;

public class JUnitSupervisor implements IRunSupervisor {
  private final SpecInfo spec;
  private final RunNotifier notifier;
  private final IStackTraceFilter filter;
  private final IRunListener masterListener;
  private final IObjectRenderer<Object> diffedObjectRenderer;

  public JUnitSupervisor(SpecInfo spec, RunNotifier notifier, IStackTraceFilter filter,
      IObjectRenderer<Object> diffedObjectRenderer) {
    this.spec = spec;
    this.notifier = notifier;
    this.filter = filter;
    this.masterListener = new MasterRunListener(spec);
    this.diffedObjectRenderer = diffedObjectRenderer;
  }

  public void beforeSpec(SpecInfo spec) {
    masterListener.beforeSpec(spec);
  }

  public void beforeFeature(FeatureInfo feature) {
    masterListener.beforeFeature(feature);

    if (!feature.isReportIterations())
      notifier.fireTestStarted(feature.getDescription());
  }

  public void beforeIteration(FeatureInfo currentFeature, IterationInfo iteration) {
    masterListener.beforeIteration(iteration);

    if (currentFeature.isReportIterations())
      notifier.fireTestStarted(iteration.getDescription());
  }


  public void error(InvokeException invokeException) {
    FeatureInfo currentFeature = invokeException.getCurrentFeature();
    IterationInfo currentIteration = invokeException.getCurrentIteration();
    ErrorInfo error = invokeException.getErrorInfo();
    error(currentFeature, currentIteration, error);
  }

  public void error(FeatureInfo currentFeature, IterationInfo currentIteration, ErrorInfo error) {
    Throwable exception = error.getException();

    if (exception instanceof MultipleFailureException) {
      handleMultipleFailures(currentFeature, currentIteration, error);
      return;
    }

    if (isFailedEqualityComparison(exception))
      exception = convertToComparisonFailure(exception);

    filter.filter(exception);

    Failure failure = new Failure(getCurrentDescription(currentFeature, currentIteration), exception);

    if (exception instanceof AssumptionViolatedException) {
      // Spock has no concept of "violated assumption", so we don't notify Spock listeners
      // do notify JUnit listeners unless it's a data-driven iteration that's reported as one feature
      if (currentIteration == null || !currentFeature.isParameterized() || currentFeature.isReportIterations()) {
        notifier.fireTestAssumptionFailed(failure);
      }
    } else {
      masterListener.error(error);
      notifier.fireTestFailure(failure);
    }
  }

  // for better JUnit compatibility, e.g when a @Rule is used
  private void handleMultipleFailures(FeatureInfo currentFeature, IterationInfo currentIteration, ErrorInfo error) {
    MultipleFailureException multiFailure = (MultipleFailureException) error.getException();
    for (Throwable failure : multiFailure.getFailures())
      error(currentFeature, currentIteration, new ErrorInfo(error.getMethod(), failure));
  }

  private boolean isFailedEqualityComparison(Throwable exception) {
    if (!(exception instanceof ConditionNotSatisfiedError)) return false;

    Condition condition = ((ConditionNotSatisfiedError) exception).getCondition();
    ExpressionInfo expr = condition.getExpression();
    return expr != null && expr.isEqualityComparison() && // it is equality
        exception.getCause() == null;    // and it is not failed because of exception
  }

  // enables IDE support (diff dialog)
  private Throwable convertToComparisonFailure(Throwable exception) {
    assert isFailedEqualityComparison(exception);

    Condition condition = ((ConditionNotSatisfiedError) exception).getCondition();
    ExpressionInfo expr = condition.getExpression();

    String actual = renderValue(expr.getChildren().get(0).getValue());
    String expected = renderValue(expr.getChildren().get(1).getValue());
    ComparisonFailure failure = new SpockComparisonFailure(condition, expected, actual);
    failure.setStackTrace(exception.getStackTrace());

    return failure;
  }

  private String renderValue(Object value) {
    try {
      return diffedObjectRenderer.render(value);
    } catch (Throwable t) {
      return "Failed to render value due to:\n\n" + TextUtil.printStackTrace(t);
    }
  }

  public void afterIteration(FeatureInfo currentFeature, IterationInfo iteration) {
    masterListener.afterIteration(iteration);
    if (currentFeature.isReportIterations()) {
      notifier.fireTestFinished(iteration.getDescription());
    }
  }

  public void noIterationFound(FeatureInfo currentFeature){
    notifier.fireTestFailure(new Failure(currentFeature.getDescription(),
      new SpockExecutionException("Data provider has no data")));
  }

  public void afterFeature(FeatureInfo feature) {
    masterListener.afterFeature(feature);
    if (!feature.isReportIterations()) {
      notifier.fireTestFinished(feature.getDescription());
    }
  }

  public void afterSpec(SpecInfo spec) {
    masterListener.afterSpec(spec);
  }

  @Override
  public void error(MultipleInvokeException multipleInvokeException) {
    for (InvokeException invokeException : multipleInvokeException.getInvokeExceptions()) {
      error(invokeException);
    }
  }

  public void specSkipped(SpecInfo spec) {
    masterListener.specSkipped(spec);
    notifier.fireTestIgnored(spec.getDescription());
  }

  public void featureSkipped(FeatureInfo feature) {
    masterListener.featureSkipped(feature);
    notifier.fireTestIgnored(feature.getDescription());
  }

  private Description getCurrentDescription(FeatureInfo currentFeature, IterationInfo currentIteration) {
    if (currentIteration != null && currentFeature.isReportIterations()) {
      return currentIteration.getDescription();
    }
    if (currentFeature != null) {
      return currentFeature.getDescription();
    }
    return spec.getDescription();
  }
}
