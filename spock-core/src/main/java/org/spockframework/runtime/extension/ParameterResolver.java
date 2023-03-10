package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.ParameterInfo;
import org.spockframework.util.Assert;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

/**
 * Helper class for resolving parameters.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
public class ParameterResolver {
  public static void resolveParameter(IMethodInvocation invocation, ParameterInfo parameterInfo, Supplier<?> valueSupplier) {
    invocation.resolveArgument(parameterInfo.getIndex(), valueSupplier.get());
  }

  /**
   * Convenience interceptor that resolves a parameter.
   */
  public static class Interceptor<T> implements IMethodInterceptor {
    private final ParameterInfo parameterInfo;
    private final Function<IMethodInvocation, T> valueSupplier;

    public Interceptor(ParameterInfo parameterInfo, Function<IMethodInvocation, T> valueSupplier) {
      this.parameterInfo = parameterInfo;
      this.valueSupplier = valueSupplier;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      resolveParameter(invocation, parameterInfo, () -> valueSupplier.apply(invocation));
      invocation.proceed();
    }
  }
}
