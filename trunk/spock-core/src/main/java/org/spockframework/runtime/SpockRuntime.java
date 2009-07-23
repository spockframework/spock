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

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import org.spockframework.runtime.model.TextPosition;
import groovy.lang.*;

/**
 * @author Peter Niederwieser
 */
public abstract class SpockRuntime {
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  static final Object VOID_RETURN_VALUE = new Object();

  public static final String VERIFY_CONDITION = "verifyCondition";

  public static void verifyCondition(ValueRecorder recorder, Object condition, String text, int line, int column) {
    // void methods are not considered implicit conditions, but we can detect them only at runtime
    if (condition == VOID_RETURN_VALUE) return;
    
    if (!DefaultTypeTransformation.castToBoolean(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), recorder, null));
  }

  public static final String VERIFY_CONDITION_WITH_MESSAGE = "verifyConditionWithMessage";

  public static void verifyConditionWithMessage(Object message, Object condition, String text, int line, int column) {
    if (!DefaultTypeTransformation.castToBoolean(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), null, DefaultGroovyMethods.toString(message)));
  }

  public static final String FEATURE_METHOD_CALLED = "featureMethodCalled";
  
  public static void featureMethodCalled() {
    throw new InvalidSpeckError("Feature methods cannot be called from user code");
  }

  public static final String NULL_AWARE_INVOKE_METHOD = "nullAwareInvokeMethod";

  /*
   * Same as InvokerHelper.invokeMethod(), except that VOID_RETURN_VALUE is
   * returned instead of null if it can be determined that the invoked method
   * has return type void. This is done on a best effort basis.
   *
   * @param target the object or class (in the case of a static method) that is
   * the target of the method call.
   *
   * @param method the method to be invoked
   * 
   * @param args the method arguments
   */
  // we don't use varargs for arguments due to http://jira.codehaus.org/browse/GROOVY-3547
  public static Object nullAwareInvokeMethod(Object target, String method, Object[] args) {
    Object returnValue = InvokerHelper.invokeMethod(target, method,  args);
    if (returnValue != null) return returnValue;

    // let's try to find the method that was invoked and see if it has return type void
    // since we now do another method dispatch (w/o actually invoking the method),
    // there is a small chance that we get an incorrect result because a MetaClass has
    // been changed since the first dispatch; to eliminate this chance we would have to
    // first find the MetaMethod and then invoke it, but the problem is that calling
    // MetaMethod.invoke doesn't have the exact same semantics as calling
    // InvokerHelper.invokeMethod, even if the same method is chosen (see Speck GroovyMopExploration)

    if (args == null) args = EMPTY_OBJECT_ARRAY;
    Class[] argClasses = new Class[args.length];
    for (int i = 0; i < args.length; i++)
      argClasses[i] = args[i] == null ? null : args[i].getClass();

    // the way we choose metaClass, we won't find methods on java.lang.Class
    // but since java.lang.Class has no void methods other than the ones inherited
    // from java.lang.Object, and since we operate on a best effort basis, that's OK
    // also we will choose a static method like Foo.getName() over the equally
    // named method on java.lang.Class, but this is consistent with current Groovy semantics
    // (see http://jira.codehaus.org/browse/GROOVY-3548)
    // in the end it's probably best to rely on NullAwareInvokeMethodSpeck to tell us if
    // everything is OK
    MetaClass metaClass = target instanceof Class ?
        InvokerHelper.getMetaClass((Class)target) : InvokerHelper.getMetaClass(target);

    // seems to find more methods than getMetaMethod()
    MetaMethod metaMethod = metaClass.pickMethod(method, argClasses);
    if (metaMethod == null) return null; // we were unable to figure out which method was called

    Class returnType = metaMethod.getReturnType();
    // although Void.class will occur rarely, it makes sense to handle
    // it in the same way as void.class
    if (returnType == void.class || returnType == Void.class) return VOID_RETURN_VALUE;

    return null;
  }

  public static final String NULL_AWARE_INVOKE_METHOD_SAFE = "nullAwareInvokeMethodSafe";

  public static Object nullAwareInvokeMethodSafe(Object target, String method, Object[] args) {
    return target == null ? null : nullAwareInvokeMethod(target, method, args);
  }
}
