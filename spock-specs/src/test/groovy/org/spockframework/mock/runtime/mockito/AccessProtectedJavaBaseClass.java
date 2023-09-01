package org.spockframework.mock.runtime.mockito;

class AccessProtectedJavaBaseClass {
  protected boolean accessible = true;
}

class AccessProtectedJavaSubClass extends AccessProtectedJavaBaseClass {
  boolean accessNonStaticFlag() {
    return accessible;
  }
}
