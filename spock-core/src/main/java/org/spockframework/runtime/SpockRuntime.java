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

import java.util.*;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;
import org.spockframework.util.*;

/**
 * @author Peter Niederwieser
 */
public abstract class SpockRuntime {
  public static final String VERIFY_CONDITION = "verifyCondition";

  // condition can be null too, but not in the sense of "not available"
  public static void verifyCondition(@Nullable ValueRecorder recorder,
      @Nullable String text, int line, int column, @Nullable Object message, Object condition) {
    if (!GroovyRuntimeUtil.isTruthy(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(recorder, text, TextPosition.create(line, column), messageToString(message)));
  }

  public static final String VERIFY_METHOD_CONDITION = "verifyMethodCondition";

  // method calls with spread-dot operator are not rewritten, hence this method doesn't have to care about spread-dot
  public static void verifyMethodCondition(@Nullable ValueRecorder recorder, @Nullable String text, int line, int column,
      @Nullable Object message, Object target, String method, Object[] args, boolean safe, boolean explicit) {
    MatcherCondition matcherCondition = MatcherCondition.parse(target, method, args, safe);
    if (matcherCondition != null) {
      matcherCondition.verify(recorder, text, line, column, messageToString(message));
      return;
    }

    Object result = safe ? InvokerHelper.invokeMethodSafe(target, method, args)
        : InvokerHelper.invokeMethod(target, method, args);

    if (recorder != null) {
      recorder.replaceLastValue(result);
    }
    
    if (!explicit && result == null && GroovyRuntimeUtil.isVoidMethod(target, method, args)) return;

    if (!GroovyRuntimeUtil.isTruthy(result))
      throw new ConditionNotSatisfiedError(
          new Condition(recorder, text, TextPosition.create(line, column), messageToString(message)));
  }

  public static final String DESPREAD_LIST = "despreadList";

  /**
   * Wrapper around ScriptBytecodeAdapter.despreadList() to avoid a direct
   * dependency on the latter.
   */
  public static Object[] despreadList(Object[] args, Object[] spreads, int[] positions) {
    return ScriptBytecodeAdapter.despreadList(args, spreads, positions);
  }

  public static final String FEATURE_METHOD_CALLED = "featureMethodCalled";

  public static void featureMethodCalled() {
    throw new InvalidSpecException("Feature methods cannot be called from user code");
  }

  private static String messageToString(Object message) {
    if (message == null) return null; // treat as "not available"

    return GroovyRuntimeUtil.toString(message);
  }
  /**
   * A condition of the form "foo equalTo(bar)" or "that(foo, equalTo(bar)",
   * where 'equalTo' returns a Hamcrest matcher.
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

    void verify(@Nullable ValueRecorder recorder, @Nullable String text, int line, int column, @Nullable String message) {
      if (HamcrestFacade.matches(matcher, actual)) return;

      if (recorder != null) {
        recorder.replaceLastValue(shortSyntax ? actual : false);
        replaceMatcherValues(recorder);
      }

      String description = HamcrestFacade.getFailureDescription(matcher, actual, message);
      Condition condition = new Condition(recorder, text, TextPosition.create(line, column), description);
      throw new ConditionNotSatisfiedError(condition);
    }

    void replaceMatcherValues(ValueRecorder recorder) {
      boolean firstOccurrence = true;
      List<Object> values = recorder.getRecordedValues();
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

      if (method.equals("that")) {
        if (target != spock.util.matcher.HamcrestSupport.class) return null;
        if (args.length != 2 || !HamcrestFacade.isMatcher(args[1])) return null;
        return new MatcherCondition(args[0], args[1], false);
      }

      return null;
    }
  }
}
