package org.spockframework.mock.runtime;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.util.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CglibMockFactory {

  private static final Class<?>[] CLASSES = new Class<?>[0];

  static Object createMock(Class<?> type, List<Class<?>> additionalInterfaces, @Nullable List<Object> constructorArgs,
                           IProxyBasedMockInterceptor interceptor, ClassLoader classLoader, boolean useObjenesis) {
    Enhancer enhancer = new ConstructorFriendlyEnhancer();
    enhancer.setClassLoader(classLoader);
    enhancer.setSuperclass(type);
    List<Class<?>> interfaces = new ArrayList<>(additionalInterfaces);
    interfaces.add(ISpockMockObject.class);
    enhancer.setInterfaces(interfaces.toArray(CLASSES));
    enhancer.setCallbackFilter(BridgeMethodAwareCallbackFilter.INSTANCE);
    MethodInterceptor cglibInterceptor = new CglibMockInterceptorAdapter(interceptor);
    enhancer.setCallbackTypes(new Class[] {cglibInterceptor.getClass(), NoOp.class});

    Class<?> enhancedType = enhancer.createClass();
    Object proxy = MockInstantiator.instantiate(type, enhancedType, constructorArgs, useObjenesis);
    ((Factory) proxy).setCallbacks(new Callback[] {cglibInterceptor, NoOp.INSTANCE});
    return proxy;
  }

  static class ConstructorFriendlyEnhancer extends Enhancer {
    @Override
    protected void filterConstructors(Class clazz, List constructors) {} // implement all ctors found in superclass
  }

  static class BridgeMethodAwareCallbackFilter implements CallbackFilter {
    // important to use same instance every time; otherwise, CGLIB will
    // keep creating new classes rather than reusing previously generated ones
    static BridgeMethodAwareCallbackFilter INSTANCE = new BridgeMethodAwareCallbackFilter();

    /**
     * @return 0 if the method should be intercepted; 1 otherwise
     */
    @Override
    public int accept(Method method) {
      // All non-bridge methods are intercepted
      if(!method.isBridge()) return 0;

      // Bridge methods are not intercepted unless they override a concrete method in a supertype (issue #122)
      Class[] prmTypes = method.getParameterTypes();
      String methodName = method.getName();
      Class<?> superclass = method.getDeclaringClass().getSuperclass();
      while(superclass != null) {
        for(Method m : superclass.getDeclaredMethods()) {
          if(!methodName.equals(m.getName())) continue;
          if(Arrays.equals(prmTypes, m.getParameterTypes())) {
            int modifiers = m.getModifiers();
            if(Modifier.isAbstract(modifiers)) return 1;
            return (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) ? 0 : 1;
          }
        }
        superclass = superclass.getSuperclass();
      }
      return 1;
    }
  }

}
