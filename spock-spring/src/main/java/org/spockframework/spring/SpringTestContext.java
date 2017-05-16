package org.spockframework.spring;

import org.spockframework.util.ReflectionUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;

import java.lang.reflect.Method;

/**
 * Wrapper around Spring's TestContext class that works with Spring 2.5 and Spring 3.
 */
public class SpringTestContext {
  private static final Method getApplicationContextMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getApplicationContext");
  private static final Method getTestInstanceMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getTestInstance");
  private static final Method setAttributeMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "setAttribute", String.class, Object.class);
  private static final Method getAttributeMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getAttribute", String.class);

  private final TestContext delegate;

  public SpringTestContext(TestContext testContext) {
    delegate = testContext;
  }

  public ApplicationContext getApplicationContext() {
    if (getApplicationContextMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getApplicationContext()' was not found");
    }
    return (ApplicationContext) ReflectionUtil.invokeMethod(delegate, getApplicationContextMethod);
  }

  public Object getTestInstance() {
    if (getTestInstanceMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getTestInstance()' was not found");
    }
    return ReflectionUtil.invokeMethod(delegate, getTestInstanceMethod);
  }

  public void setAttribute(String name, Object value) {
    if (setAttributeMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.setAttribute()' was not found");
    }
    ReflectionUtil.invokeMethod(delegate, setAttributeMethod, name, value);
  }

  public Object getAttribute(String name) {
    if (getAttributeMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getAttribute()' was not found");
    }
    return ReflectionUtil.invokeMethod(delegate, getAttributeMethod, name);
  }
}
