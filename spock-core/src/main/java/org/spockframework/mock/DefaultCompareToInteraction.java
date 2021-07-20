package org.spockframework.mock;

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
      && method.getParameterTypes().get(0) == Object.class
      && invocation.getMockObject().getInstance() instanceof Comparable;
  }

  @Override
  public Object accept(IMockInvocation invocation) {
    return Integer.compare(System.identityHashCode(invocation.getMockObject().getInstance()), System.identityHashCode(invocation.getArguments().get(0)));
  }
}
