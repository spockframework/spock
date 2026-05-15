package org.spockframework.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class DefaultCompareToInteraction extends DefaultInteraction {
  public static final DefaultCompareToInteraction INSTANCE = new DefaultCompareToInteraction();

  private DefaultCompareToInteraction() {}

  @Override
  public String getText() {
    return "Comparable.compareTo() interaction";
  }

  @Override
  public boolean matches(IMockInvocation invocation) {
    IMockMethod method = invocation.getMethod();

    return "compareTo".equals(method.getName())
      && method.getParameterTypes().size() == 1
      && (
        // either this is the base compareTo(object) method or it is a genericized Comparable
        method.getParameterTypes().get(0) == Object.class
        || getInterfacesFromClass(method.getParameterTypes().get(0)).contains(Comparable.class)
      ) && invocation.getMockObject().getInstance() instanceof Comparable;
  }

  @Override
  public Supplier<Object> accept(IMockInvocation invocation) {
    return () -> Integer.compare(System.identityHashCode(invocation.getMockObject().getInstance()), System.identityHashCode(invocation.getArguments().get(0)));
  }

  private Set<Class<?>> getInterfacesFromClass(Class<?> clazz) {
    Set<Class<?>> interfaces = new HashSet<>();
    if (clazz.isInterface()) {
      interfaces.add(clazz);
    }
    for (Class<?> iface : clazz.getInterfaces()) {
      interfaces.addAll(getInterfacesFromClass(iface));
    }
    if (clazz.getSuperclass() != null) {
      interfaces.addAll(getInterfacesFromClass(clazz.getSuperclass()));
    }
    return interfaces;
  }

}
