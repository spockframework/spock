/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime;

import static java.util.stream.Collectors.toList;
import static org.spockframework.util.ReflectionUtil.isArray;

import org.spockframework.runtime.model.*;
import org.spockframework.util.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

import groovy.lang.Closure;
import org.hamcrest.*;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

/**
 * @author Peter Niederwieser
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class SpockRuntime {
  public static final String VERIFY_CONDITION = "verifyCondition";

  // condition can be null too, but not in the sense of "not available"
  public static void verifyCondition(ErrorCollector errorCollector, @Nullable ValueRecorder recorder,
      @Nullable String text, int line, int column, @Nullable Object message, @Nullable Object condition) {
    if (!GroovyRuntimeUtil.isTruthy(condition)) {
      final ConditionNotSatisfiedError conditionNotSatisfiedError = new ConditionNotSatisfiedError(
        new Condition(getValues(recorder), text, TextPosition.create(line, column), messageToString(message), null, null));
      errorCollector.collectOrThrow(conditionNotSatisfiedError);
    }
  }

  public static final String CONDITION_FAILED_WITH_EXCEPTION = "conditionFailedWithException";

  public static void conditionFailedWithException(ErrorCollector errorCollector, @Nullable ValueRecorder recorder, @Nullable String text, int line, int column, @Nullable Object message, Throwable throwable){
      if (throwable instanceof SpockAssertionError) {
        final SpockAssertionError spockAssertionError = (SpockAssertionError) throwable;
        errorCollector.collectOrThrow(spockAssertionError); // this is our exception - it already has good message
        return;
      }
      if (throwable instanceof SpockException) {
        final SpockException spockException = (SpockException) throwable;
        errorCollector.collectOrThrow(spockException); // this is our exception - it already has good message
        return;
      }
    final ConditionFailedWithExceptionError conditionNotSatisfiedError = new ConditionFailedWithExceptionError(
        new Condition(
            getValues(recorder),
            text,
            TextPosition.create(line, column),
            messageToString(message),
            recorder == null ? null : recorder.getCurrentRecordingVarNum(),
            recorder == null ? null : throwable),
        throwable);
    errorCollector.collectOrThrow(conditionNotSatisfiedError);
  }

  public static final String GROUP_CONDITION_FAILED_WITH_EXCEPTION = "groupConditionFailedWithException";

  public static void groupConditionFailedWithException(ErrorCollector errorCollector, Throwable throwable){
    if (throwable instanceof AssertionError) {
      final AssertionError assertionError = (AssertionError) throwable;
      errorCollector.collectOrThrow(assertionError); // this is our exception - it already has good message
      return;
    }
    if (throwable instanceof SpockException) {
      final SpockException spockException = (SpockException) throwable;
      errorCollector.collectOrThrow(spockException); // this is our exception - it already has good message
      return;
    }
    ExceptionUtil.sneakyThrow(throwable);
  }

  public static final String VERIFY_METHOD_CONDITION = "verifyMethodCondition";

  // method calls with spread-dot operator are not rewritten, hence this method doesn't have to care about spread-dot
  public static void verifyMethodCondition(ErrorCollector errorCollector, @Nullable ValueRecorder recorder, @Nullable String text, int line, int column,
      @Nullable Object message, Object target, String method, Object[] args, boolean safe, boolean explicit, int lastVariableNum) {
    MatcherCondition matcherCondition = MatcherCondition.parse(target, method, args, safe);
    if (matcherCondition != null) {
      matcherCondition.verify(errorCollector, getValues(recorder), text, line, column, messageToString(message));
      return;
    }

    CollectionCondition collectionCondition = CollectionCondition.parse(target, method, args, safe);
    if (collectionCondition != null) {
      collectionCondition.verify(errorCollector, getValues(recorder), text, line, column, messageToString(message));
      return;
    }

    if (recorder != null) {
      recorder.startRecordingValue(lastVariableNum);
    }
    Object result = safe ? GroovyRuntimeUtil.invokeMethodNullSafe(target, method, args) :
        GroovyRuntimeUtil.invokeMethod(target, method, args);

    if (!explicit && result == null && isVoidMethod(target, method, args)) return;

    if (!GroovyRuntimeUtil.isTruthy(result)) {
      List<Object> values = getValues(recorder);
      if (values != null) CollectionUtil.setLastElement(values, result);
      final ConditionNotSatisfiedError conditionNotSatisfiedError = new ConditionNotSatisfiedError(
          new Condition(values, text, TextPosition.create(line, column), messageToString(message), null, null));
      errorCollector.collectOrThrow(conditionNotSatisfiedError);
    }
  }

  public static final String MATCH_COLLECTIONS_AS_SET = "matchCollectionsAsSet";

  public static void matchCollectionsAsSet(Object a, Object b) {
  }

  public static final String MATCH_COLLECTIONS_IN_ANY_ORDER = "matchCollectionsInAnyOrder";

  public static void matchCollectionsInAnyOrder(Object a, Object b) {
  }

  private static boolean isVoidMethod(@Nullable Object target, String method, Object... args) {
    if (target instanceof Closure) { // since we support verifyAll we must check the closure hierarchy
      Closure closure = ((Closure)target);
      return GroovyRuntimeUtil.isVoidMethod(closure.getDelegate(), method, args)
        || isVoidMethod(closure.getOwner(), method, args);
    }
    return GroovyRuntimeUtil.isVoidMethod(target, method, args);
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

    void verify(ErrorCollector errorCollector, @Nullable List<Object> values, @Nullable String text, int line, int column, @Nullable String message) {
      if (HamcrestFacade.matches(matcher, actual)) return;

      if (values != null) {
        CollectionUtil.setLastElement(values, shortSyntax ? actual : false);
        replaceMatcherValues(values);
      }

      String description = HamcrestFacade.getFailureDescription(matcher, actual, message);
      Condition condition = new Condition(values, text, TextPosition.create(line, column), description, null, null);
      errorCollector.collectOrThrow(new ConditionNotSatisfiedError(condition));
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

      if ("call".equals(method)) {
        if (args.length != 1 || !HamcrestFacade.isMatcher(args[0])) return null;
        return new MatcherCondition(target, args[0], true);
      }

      if ("that".equals(method) || "expect".equals(method)) {
        if (target != spock.util.matcher.HamcrestSupport.class) return null;
        if (args.length != 2 || !HamcrestFacade.isMatcher(args[1])) return null;
        return new MatcherCondition(args[0], args[1], false);
      }

      return null;
    }
  }

  private static class CollectionCondition {

    private final String method;
    private final Object left;
    private final Object right;

    public CollectionCondition(String method, Object left, Object right) {
      this.method = method;
      this.left = left;
      this.right = right;
    }

    void verify(ErrorCollector errorCollector, @Nullable List<Object> values, @Nullable String text, int line, int column, @Nullable String message) {
      String description = null;
      int idxActual = values.indexOf(left);
      int idxExpected = values.indexOf(right);


      if (isIterableOrArray(left) && isIterableOrArray(right)) {
        if (SpockRuntime.MATCH_COLLECTIONS_AS_SET.equals(method)) {
          Set<?> actual = GroovyRuntimeUtil.coerce(left, LinkedHashSet.class);
          Set<?> expected = GroovyRuntimeUtil.coerce(right, LinkedHashSet.class);
          if (GroovyRuntimeUtil.equals(actual, expected)) {
            return;
          }
          // replace arguments with coerced Set values
          values.set(idxActual, actual);
          values.set(idxExpected, expected);

        } else {
          Object localLeft = convertArrayToCollectionIfNecessary(left);
          Object localRight = convertArrayToCollectionIfNecessary(right);
          Matcher<?> matcher = StreamSupport.stream(((Iterable<?>)localRight).spliterator(), false)
            .map(CoreMatchers::equalTo)
            .collect(Collectors.collectingAndThen(toList(), IsIterableContainingInAnyOrder::containsInAnyOrder));

          if (HamcrestFacade.matches(matcher, localLeft)) {
            return;
          }
          description = HamcrestFacade.getFailureDescription(matcher, left, message);
        }
        // we have a mismatch, so add false for the result
        // due to the way the ValueRecorder works, we have two (n/a) values at the end, so we need to skip them
        values.add(values.size() - 2, false);
      } else if (left == null || right == null) {
        values.set(idxActual, left);
        values.set(idxExpected, right);
        if (left == null && right == null) {
          return;
        }
        values.set(3, false);
      } else {
        Pattern pattern = Pattern.compile(String.valueOf(right));
        java.util.regex.Matcher matcher = pattern.matcher(String.valueOf(left));

        values.set(idxActual, left);
        values.set(idxExpected, right);

        if (SpockRuntime.MATCH_COLLECTIONS_AS_SET.equals(method)) {
          if (matcher.find()) {
            return;
          }
          values.set(3, matcher);
        } else {
          if (matcher.matches()) {
            return;
          }
          values.set(3, false);
        }
      }

      // this contains the synthetic method name, so remove it
      values.remove(0);
      Condition condition = new Condition(values, text, TextPosition.create(line, column), description, null, null);
      errorCollector.collectOrThrow(new ConditionNotSatisfiedError(condition));
    }

    @Nullable
    static CollectionCondition parse(Object target, String method, Object[] args, boolean safe) {
      if (safe) return null;

      if (target == SpockRuntime.class) {
        if (SpockRuntime.MATCH_COLLECTIONS_AS_SET.equals(method) || SpockRuntime.MATCH_COLLECTIONS_IN_ANY_ORDER.equals(method)) {
          return new CollectionCondition(method, args[0], args[1]);
        }
      }
      return null;
    }
  }
  private static boolean isIterableOrArray(Object o) {
    return o instanceof Iterable || isArray(o);
  }

  private static Object convertArrayToCollectionIfNecessary(Object o) {
    if (isArray(o)) {
      return  GroovyRuntimeUtil.coerce(o, List.class);
    }
    return o;
  }
}
