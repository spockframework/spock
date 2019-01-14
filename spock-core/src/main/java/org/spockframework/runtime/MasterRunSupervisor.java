package org.spockframework.runtime;

import org.spockframework.runtime.condition.IObjectRenderer;
import org.spockframework.runtime.model.*;
import org.spockframework.util.TextUtil;

import org.junit.ComparisonFailure;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.MultipleFailureException;

import static org.spockframework.runtime.RunStatus.OK;

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
  public int error(ErrorInfo error) {
    Throwable exception = error.getException();

//    if (exception instanceof MultipleFailureException)
//      return handleMultipleFailures(error);
//
//    if (isFailedEqualityComparison(exception))
//      exception = convertToComparisonFailure(exception);

    filter.filter(exception);

    if (exception instanceof AssumptionViolatedException) {
      // Spock has no concept of "violated assumption", so we don't notify Spock listeners
      // do notify JUnit listeners unless it's a data-driven iteration that's reported as one feature
    } else {
      masterListener.error(error);
    }
    return 0;
  }

  // for better JUnit compatibility, e.g when a @Rule is used
  private int handleMultipleFailures(ErrorInfo error) {
    MultipleFailureException multiFailure = (MultipleFailureException) error.getException();
    int runStatus = OK;
    for (Throwable failure : multiFailure.getFailures())
      runStatus = error(new ErrorInfo(error.getMethod(), failure));
    return runStatus;
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

    String actual = renderValue(expr.getChildren().get(0).getValue());
    String expected = renderValue(expr.getChildren().get(1).getValue());
    // TODO replace by AssertionFailedError
    ComparisonFailure failure = new SpockComparisonFailure(condition, expected, actual);
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
