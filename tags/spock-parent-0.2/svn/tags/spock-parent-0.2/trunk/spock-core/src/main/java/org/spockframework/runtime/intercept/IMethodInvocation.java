package org.spockframework.runtime.intercept;

import org.spockframework.runtime.model.MethodInfo;

/**
 * @author Peter Niederwieser
 */
public interface IMethodInvocation {
  public MethodInfo getMethod();
  public Object[] getArguments();
  public void proceed() throws Throwable;
}
