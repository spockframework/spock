package org.spockframework.runtime;

import org.spockframework.runtime.condition.IObjectRenderer;
import org.spockframework.runtime.model.*;
import org.spockframework.util.TextUtil;

import org.opentest4j.*;

class MasterRunSupervisor implements IRunSupervisor {

  private final IStackTraceFilter filter;
  private final IRunListener masterListener;
  private final IObjectRenderer<Object> diffedObjectRenderer;

  MasterRunSupervisor(SpecInfo specInfo, IStackTraceFilter filter,
                      IObjectRenderer<Object> diffedObjectRenderer) {
    masterListener = new MasterRunListener(specInfo);
    this.filter = filter;
    this.diffedObjectRenderer = diffedObjectRenderer;
  }

  @Override
  public void beforeSpec(SpecInfo spec) {
    masterListener.beforeSpec(spec);
  }

  @Override
  public void beforeFeature(FeatureInfo feature) {
    masterListener.beforeFeature(feature);
  }

  @Override
  public void beforeIteration(IterationInfo iteration) {
    masterListener.beforeIteration(iteration);
  }

  @Override
  public void error(ErrorInfoCollector errorInfoCollector, ErrorInfo error) {
    Throwable exception = error.getException();

    if (exception instanceof MultipleFailuresError) {
      handleMultipleFailures(errorInfoCollector, error);
      return;
    }

    exception = transform(exception);

    ErrorInfo transformedError = new ErrorInfo(error.getMethod(), exception, error.getErrorContext());
    if (exception instanceof TestAbortedException || exception instanceof TestSkippedException) {
      // Spock has no concept of "aborted tests", so we don't notify Spock listeners
    } else {
      masterListener.error(transformedError);
    }
    errorInfoCollector.addErrorInfo(transformedError);
  }

  // for better JUnit compatibility, e.g when a @Rule is used
  private void handleMultipleFailures(ErrorInfoCollector errorInfoCollector,  ErrorInfo error) {
    MultipleFailuresError multiFailure = (MultipleFailuresError) error.getException();
    for (Throwable failure : multiFailure.getFailures())
      error(errorInfoCollector, new ErrorInfo(error.getMethod(), failure, error.getErrorContext()));
  }


  private Throwable transform(Throwable throwable) {
    if (isFailedEqualityComparison(throwable))
      throwable = convertToComparisonFailure(throwable);

    filter.filter(throwable);
    return throwable;
  }

  private boolean isFailedEqualityComparison(Throwable exception) {
    if (!(exception instanceof ConditionNotSatisfiedError)) return false;

    ConditionNotSatisfiedError conditionNotSatisfiedError = (ConditionNotSatisfiedError) exception;
    Condition condition = conditionNotSatisfiedError.getCondition();
    ExpressionInfo expr = condition.getExpression();
    return expr != null && expr.isEqualityComparison() && // it is equality
      conditionNotSatisfiedError.getCause() == null;    // and it is not failed because of exception
  }

  // enables IDE support (diff dialog)
  private Throwable convertToComparisonFailure(Throwable exception) {
    assert isFailedEqualityComparison(exception);

    ConditionNotSatisfiedError conditionNotSatisfiedError = (ConditionNotSatisfiedError) exception;
    Condition condition = conditionNotSatisfiedError.getCondition();
    ExpressionInfo expr = condition.getExpression();

    Object actualValue = expr.getChildren().get(0).getValue();
    ValueWrapper actual = ValueWrapper.create(actualValue, renderValue(actualValue));

    Object expectedValue = expr.getChildren().get(1).getValue();
    ValueWrapper expected = ValueWrapper.create(expectedValue, renderValue(expectedValue));

    AssertionFailedError failure = new SpockComparisonFailure(condition, expected, actual);
    failure.setStackTrace(exception.getStackTrace());

    if (conditionNotSatisfiedError.getCause()!=null){
      failure.initCause(conditionNotSatisfiedError.getCause());
    }

    return failure;
  }

  private String renderValue(Object value) {
    try {
      return diffedObjectRenderer.render(value);
    } catch (Throwable t) {
      return "Failed to render value due to:\n\n" + TextUtil.printStackTrace(t);
    }
  }


  @Override
  public void afterIteration(IterationInfo iteration) {
    masterListener.afterIteration(iteration);
  }

  @Override
  public void afterFeature(FeatureInfo feature) {
    masterListener.afterFeature(feature);
  }

  @Override
  public void afterSpec(SpecInfo spec) {
    masterListener.afterSpec(spec);
  }

  @Override
  public void specSkipped(SpecInfo spec) {
    masterListener.specSkipped(spec);
  }

  @Override
  public void featureSkipped(FeatureInfo feature) {
    masterListener.featureSkipped(feature);
  }
}
