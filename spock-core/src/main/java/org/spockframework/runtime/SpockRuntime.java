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

import spock.lang.Specification;

/**
 * @author Peter Niederwieser
 */
public abstract class SpockRuntime {
  public static final String VERIFY_CONDITION = "verifyCondition";

  public static void verifyCondition(ValueRecorder recorder, String text, int line, int column, Object condition) {
    if (!GroovyRuntimeUtil.isTruthy(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), recorder, null));
  }

  public static final String VERIFY_MESSAGE_CONDITION = "verifyMessageCondition";

  public static void verifyMessageCondition(ValueRecorder recorder, String text, int line, int column,
      Object condition, Object message) {
    if (!GroovyRuntimeUtil.isTruthy(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), null, GroovyRuntimeUtil.toString(message)));
  }

  public static final String VERIFY_METHOD_CONDITION = "verifyMethodCondition";

  // method calls with spread-dot operator are not rewritten, hence this method doesn't have to care about spread-dot
  public static void verifyMethodCondition(ValueRecorder recorder, String text, int line, int column,
      Object target, String method, Object[] args, boolean safe, boolean explicit) {
    MatcherCondition matcherCondition = MatcherCondition.parse(target, method, args, safe);
    if (matcherCondition != null) {
      matcherCondition.verify(recorder, text, line, column);
      return;
    }

    Object result = safe ? InvokerHelper.invokeMethodSafe(target, method, args)
        : InvokerHelper.invokeMethod(target, method, args);

    recorder.replaceLastValue(result);
    
    if (!explicit && result == null && GroovyRuntimeUtil.isVoidMethod(target, method, args)) return;

    if (!GroovyRuntimeUtil.isTruthy(result))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), recorder, null));
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

  /**
   * A condition of the form "foo equalTo(bar)" or "that(foo, equalTo(bar)",
   * where 'equalTo' returns a Hamcrest matcher.
   */
  private static class MatcherCondition {
    final Object actual;
    final Object matcher;
    final boolean implicit; // true iff the short "foo equalTo(bar)" syntax is used

    MatcherCondition(Object actual, Object matcher, boolean implicit) {
      this.actual = actual;
      this.matcher = matcher;
      this.implicit = implicit;
    }

    void verify(ValueRecorder recorder, String text, int line, int column) {
      if (HamcrestSupport.matches(matcher, actual)) return;

      recorder.replaceLastValue(implicit ? actual : false);
      replaceMatcherValues(recorder);

      String description = HamcrestSupport.getFailureDescription(matcher, actual);
      Condition condition = new Condition(text, TextPosition.create(line, column), recorder, description);
      throw new ConditionNotSatisfiedError(condition);
    }

    void replaceMatcherValues(ValueRecorder recorder) {
      boolean firstOccurrence = true;
      List<Object> values = recorder.getRecordedValues();
      ListIterator<Object> iter = values.listIterator(values.size());

      while (iter.hasPrevious()) {
        Object value = iter.previous();
        if (!HamcrestSupport.isMatcher(value)) continue;

        if (firstOccurrence) {
          // indicate mismatch in condition output
          iter.set(implicit ? false : ExpressionInfo.VALUE_NOT_AVAILABLE);
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
        if (args.length != 1 || !HamcrestSupport.isMatcher(args[0])) return null;
        return new MatcherCondition(target, args[0], true);
      }

      if (method.equals("that")) {
        if (!(target instanceof Specification)) return null;
        if (args.length != 2 || !HamcrestSupport.isMatcher(args[1])) return null;
        return new MatcherCondition(args[0], args[1], false);
      }

      return null;
    }
  }
}
