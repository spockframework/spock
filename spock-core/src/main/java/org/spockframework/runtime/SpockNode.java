package org.spockframework.runtime;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.InternalSpockError;

import spock.lang.Specification;

public abstract class SpockNode extends AbstractTestDescriptor implements Node<SpockExecutionContext> {

  protected SpockNode(UniqueId uniqueId, String displayName) {
    super(uniqueId, displayName);
  }

  protected SpockNode(UniqueId uniqueId, String displayName, TestSource source) {
    super(uniqueId, displayName, source);
  }

  protected Specification createSpecInstance(SpecInfo specInfo) {
    try {
      return (Specification) specInfo.getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(specInfo.getName());
    }
  }

  protected void invoke(SpockExecutionContext executionContext, Object target, MethodInfo method, Object... arguments) {
    if (method == null || method.isExcluded()) {
      return;
    }
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(null,
      null, executionContext.getSharedInstance(), executionContext.getCurrentInstance(), target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable throwable) {
      ExceptionUtil.sneakyThrow(throwable);
    }
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object... arguments) {
    try {
      return method.invoke(target, arguments);
    } catch (Throwable throwable) {
      ExceptionUtil.sneakyThrow(throwable);
      return null; // never happens tm
    }
  }
}
