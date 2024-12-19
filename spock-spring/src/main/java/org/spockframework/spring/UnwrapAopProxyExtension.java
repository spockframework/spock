/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;

public class UnwrapAopProxyExtension implements IStatelessAnnotationDrivenExtension<UnwrapAopProxy> {

  @Override
  public void visitFieldAnnotation(UnwrapAopProxy annotation, final FieldInfo field) {
    field.getParent().addSetupInterceptor(new UnwrapMethodInterceptor(field));
  }

  /**
   * Taken from {@link org.springframework.test.util.AopTestUtils#getUltimateTargetObject(java.lang.Object)}
   * and copied to provide support for versions earlier then Spring 4.2
   *
   * Get the ultimate <em>target</em> object of the supplied {@code candidate}
   * object, unwrapping not only a top-level proxy but also any number of
   * nested proxies.
   * <p>If the supplied {@code candidate} is a Spring
   * {@linkplain AopUtils#isAopProxy proxy}, the ultimate target of all
   * nested proxies will be returned; otherwise, the {@code candidate}
   * will be returned <em>as is</em>.
   * @param candidate the instance to check (potentially a Spring AOP proxy;
   * never {@code null})
   * @return the target object or the {@code candidate} (never {@code null})
   * @throws IllegalStateException if an error occurs while unwrapping a proxy
   * @see Advised#getTargetSource()
   * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass
   */
  public static <T> T getUltimateTargetObject(Object candidate) {
    Assert.notNull(candidate, "Candidate must not be null");
    try {
      if (AopUtils.isAopProxy(candidate) && (candidate instanceof Advised)) {
        Object target = ((Advised) candidate).getTargetSource().getTarget();
        if (target != null) {
          return (T) getUltimateTargetObject(target);
        }
      }
    }
    catch (Throwable ex) {
      throw new IllegalStateException("Failed to unwrap proxied object", ex);
    }
    return (T) candidate;
  }

  private static class UnwrapMethodInterceptor implements IMethodInterceptor {
    private final FieldInfo field;

    public UnwrapMethodInterceptor(FieldInfo field) {
      this.field = field;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      Object value = field.readValue(invocation.getInstance());
      value = getUltimateTargetObject(value);
      if (value != null) {
        field.writeValue(invocation.getInstance(), value);
      }
      invocation.proceed();
    }
  }
}
