package org.spockframework.util;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.builtin.PreconditionContext;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class ConditionUtil {

  public static Closure createCondition(Class<? extends Closure> clazz) {
    try {
      return clazz.getConstructor(Object.class, Object.class).newInstance(null, null);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate condition", e);
    }
  }

  public static Object evaluateCondition(Closure condition) {
    return evaluateCondition(condition, emptyMap());
  }

  public static Object evaluateCondition(Closure condition, Map<String, Object> dataVariables) {
    PreconditionContext context = new PreconditionContext(dataVariables);
    condition.setDelegate(context);
    condition.setResolveStrategy(Closure.DELEGATE_ONLY);

    try {
      return condition.call(context);
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate condition", e);
    }
  }

}
