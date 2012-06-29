package org.spockframework.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.*;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisException;
import org.objenesis.ObjenesisStd;
import org.spockframework.util.ReflectionUtil;

public class ProxyBasedMockFactory implements IMockFactory {
  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");
  private static final boolean objenesisAvailable = ReflectionUtil.isClassAvailable("org.objenesis.Objenesis");

  private final IProxyBasedMockInterceptor mockInterceptor;
  private final boolean forceCglib;

  public ProxyBasedMockFactory(IProxyBasedMockInterceptor mockInterceptor, boolean forceCglib) {
    this.mockInterceptor = mockInterceptor;
    this.forceCglib = forceCglib;
  }

  public Object create(MockSpec mockSpec, IInvocationDispatcher dispatcher) throws CannotCreateMockException {
    Object proxy;

    if (mockSpec.getType().isInterface() && !forceCglib) {
      proxy = createDynamicProxyMock(mockSpec);
    } else if (cglibAvailable) {
      proxy = CglibMockFactory.createMock(mockSpec, mockInterceptor);
    } else {
      if (forceCglib) {
        throw new CannotCreateMockException(mockSpec, "CGLIB was forced but not found on the class path. "
            + "To solve this problem, put cglib-nodep-2.2 or higher on the class path.");
      } else {
        throw new CannotCreateMockException(mockSpec,
            "mocking of classes (rather than interfaces) requires CGLIB. "
                + "To solve this problem, put cglib-nodep-2.2 or higher on the class path."
        );
      }
    }

    return proxy;
  }

  private Object createDynamicProxyMock(MockSpec mockSpec) {
    return Proxy.newProxyInstance(
        mockSpec.getType().getClassLoader(),
        new Class<?>[]{mockSpec.getType()},
        new DynamicProxyMockInterceptorAdapter(mockInterceptor)
    );
  }

  // inner class to defer class loading
  private static class CglibMockFactory {
    static Object createMock(MockSpec mockSpec, IProxyBasedMockInterceptor mockInterceptor) {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(mockSpec.getType());
      enhancer.setCallbackFilter(new CallbackFilter() {
        public int accept(Method method) {
          return method.isBridge() ? 1 : 0;
        }
      });

      MethodInterceptor cglibInterceptor = new CglibMockInterceptorAdapter(mockInterceptor);

      if (objenesisAvailable) {
        enhancer.setCallbackTypes(new Class[] {cglibInterceptor.getClass(), NoOp.class});
        Object proxy = ObjenesisInstantiator.instantiate(mockSpec, enhancer.createClass());
        ((Factory) proxy).setCallbacks(new Callback[] {cglibInterceptor, NoOp.INSTANCE});
        return proxy;
      } else {
        try {
          enhancer.setCallbacks(new Callback[] {cglibInterceptor, NoOp.INSTANCE});
          return enhancer.create(); // throws what if no parameterless superclass constructor available?
        } catch (Exception e) {
          throw new CannotCreateMockException(mockSpec,
              "mocking of classes without parameterless constructor requires Objenesis. "
                  + "To solve this problem, put objenesis-1.2 or higher on the classpath."
          );
        }
      }
    }

    // inner class to defer class loading
    private static class ObjenesisInstantiator {
      static final Objenesis objenesis = new ObjenesisStd();

      static Object instantiate(MockSpec mockSpec, Class<?> actualType) {
        try {
          return objenesis.newInstance(actualType);
        } catch (ObjenesisException e) {
          throw new CannotCreateMockException(mockSpec, e);
        }
      }
    }
  }
}


