/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.junit.runners.model.MultipleFailureException;
import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.Nullable;
import spock.lang.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Peter Niederwieser
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class SpockRuntime {
  private static final ThreadLocal<List<Throwable>> ERROR_COLLECTOR_THREAD_LOCAL = new ThreadLocal<List<Throwable>>();

  public static final String VERIFY_CONDITION = "verifyCondition";

  // condition can be null too, but not in the sense of "not available"
  public static void verifyCondition(@Nullable ValueRecorder recorder,
      @Nullable String text, int line, int column, @Nullable Object message, @Nullable Object condition) {
    if (!GroovyRuntimeUtil.isTruthy(condition)) {
      throw new ConditionNotSatisfiedError(
          new Condition(getValues(recorder), text, TextPosition.create(line, column), messageToString(message), null, null));
    }
  }

  public static final String CONDITION_FAILED_WITH_EXCEPTION = "conditionFailedWithException";

  public static void conditionFailedWithException(@Nullable ValueRecorder recorder, @Nullable String text, int line, int column, @Nullable Object message, Throwable throwable){
      if (throwable instanceof SpockAssertionError) {
        final SpockAssertionError spockAssertionError = (SpockAssertionError) throwable;
        collectOrThrow(spockAssertionError); // this is our exception - it already has good message
        return;
      }
      if (throwable instanceof SpockException) {
        final SpockException spockException = (SpockException) throwable;
        collectOrThrow(spockException); // this is our exception - it already has good message
        return;
      }
    final ConditionNotSatisfiedError conditionNotSatisfiedError = new ConditionNotSatisfiedError(
        new Condition(
            getValues(recorder),
            text,
            TextPosition.create(line, column),
            messageToString(message),
            recorder == null ? null : recorder.getCurrentRecordingVarNum(),
            recorder == null ? null : throwable),
        throwable);
    collectOrThrow(conditionNotSatisfiedError);
  }

  public static final String VERIFY_METHOD_CONDITION = "verifyMethodCondition";

  // method calls with spread-dot operator are not rewritten, hence this method doesn't have to care about spread-dot
  public static void verifyMethodCondition(@Nullable ValueRecorder recorder, @Nullable String text, int line, int column,
      @Nullable Object message, Object target, String method, Object[] args, boolean safe, boolean explicit, int lastVariableNum) {
    MatcherCondition matcherCondition = MatcherCondition.parse(target, method, args, safe);
    if (matcherCondition != null) {
      matcherCondition.verify(getValues(recorder), text, line, column, messageToString(message));
      return;
    }

    if (recorder != null) {
      recorder.startRecordingValue(lastVariableNum);
    }
    Object result = safe ? GroovyRuntimeUtil.invokeMethodNullSafe(target, method, args) :
        GroovyRuntimeUtil.invokeMethod(target, method, args);

    if (!explicit && result == null && GroovyRuntimeUtil.isVoidMethod(target, method, args)) return;

    if (!GroovyRuntimeUtil.isTruthy(result)) {
      List<Object> values = getValues(recorder);
      if (values != null) CollectionUtil.setLastElement(values, result);
      final ConditionNotSatisfiedError conditionNotSatisfiedError = new ConditionNotSatisfiedError(
          new Condition(values, text, TextPosition.create(line, column), messageToString(message), null, null));
      collectOrThrow(conditionNotSatisfiedError);
    }
  }

  public static final String VERIFY_COLLECTED_ERRORS = "verifyCollectedErrors";

  public static void verifyCollectedErrors() throws Throwable {
    final List<Throwable> errorCollector = ERROR_COLLECTOR_THREAD_LOCAL.get();
    if (errorCollector != null) {
      MultipleFailureException.assertEmpty(errorCollector);
    }
  }

  public static final String SETUP_ERROR_COLLECTOR = "setupErrorCollector";

  public static void setupErrorCollector(Specification specification){
    final boolean errorCollectionEnabled = specification.getSpecificationContext().getCurrentFeature().isErrorCollectionEnabled();
    if (errorCollectionEnabled){
      ERROR_COLLECTOR_THREAD_LOCAL.set(new ArrayList<Throwable>());
    }else {
      ERROR_COLLECTOR_THREAD_LOCAL.remove();
    }
  }

  public static final String DESPREAD_LIST = "despreadList";

  public static Object[] despreadList(Object[] args, Object[] spreads, int[] positions) {
    return GroovyRuntimeUtil.despreadList(args, spreads, positions);
  }

  private static List<Object> getValues(ValueRecorder recorder) {
      return recorder == null ? null : recorder.getValues();
  }

  private static String messageToString(Object message) {
    if (message == null) return null; // treat as "not available"

    return GroovyRuntimeUtil.toString(message);
  }

  private static <T extends Throwable> void collectOrThrow(T throwable) throws T {
    final List<Throwable> errorCollector = ERROR_COLLECTOR_THREAD_LOCAL.get();
    if (errorCollector != null) {
      errorCollector.add(throwable);
    } else {
      throw throwable;
    }
  }
  /**
   * A condition of the form "foo equalTo(bar)" or "that(foo, equalTo(bar)"
   * or "expect(foo, equalTo(bar)", where 'equalTo' returns a Hamcrest matcher.
   */
  private static class MatcherCondition {
    final Object actual;
    final Object matcher;
    // true if the "foo equalTo(bar)" syntax is used,
    // false if the "that(foo, equalTo(bar)" syntax is used
    final boolean shortSyntax;

    MatcherCondition(Object actual, Object matcher, boolean shortSyntax) {
      this.actual = actual;
      this.matcher = matcher;
      this.shortSyntax = shortSyntax;
    }

    void verify(@Nullable List<Object> values, @Nullable String text, int line, int column, @Nullable String message) {
      if (HamcrestFacade.matches(matcher, actual)) return;

      if (values != null) {
        CollectionUtil.setLastElement(values, shortSyntax ? actual : false);
        replaceMatcherValues(values);
      }

      String description = HamcrestFacade.getFailureDescription(matcher, actual, message);
      Condition condition = new Condition(values, text, TextPosition.create(line, column), description, null, null);
      collectOrThrow(new ConditionNotSatisfiedError(condition));
    }

    void replaceMatcherValues(List<Object> values) {
      boolean firstOccurrence = true;
      ListIterator<Object> iter = values.listIterator(values.size());

      while (iter.hasPrevious()) {
        Object value = iter.previous();
        if (!HamcrestFacade.isMatcher(value)) continue;

        if (firstOccurrence) {
          // indicate mismatch in condition output
          iter.set(shortSyntax ? false : ExpressionInfo.VALUE_NOT_AVAILABLE);
          firstOccurrence = false;
        } else {
          // don't show in condition output
          iter.set(ExpressionInfo.VALUE_NOT_AVAILABLE);
        }
      }
    }

    @Nullable
    static MatcherCondition parse(Object target, String method, Object[] args, boolean safe) {
      if (safe) return null;

      if (method.equals("call")) {
        if (args.length != 1 || !HamcrestFacade.isMatcher(args[0])) return null;
        return new MatcherCondition(target, args[0], true);
      }

      if (method.equals("that") || method.equals("expect")) {
        if (target != spock.util.matcher.HamcrestSupport.class) return null;
        if (args.length != 2 || !HamcrestFacade.isMatcher(args[1])) return null;
        return new MatcherCondition(args[0], args[1], false);
      }

      return null;
    }
  }
}
