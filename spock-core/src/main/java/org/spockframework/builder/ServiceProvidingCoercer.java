package org.spockframework.builder;

import java.lang.reflect.Type;

public class ServiceProvidingCoercer implements ITypeCoercer {
  private Object service;
  private Type serviceType;

  public ServiceProvidingCoercer(Object service, Type serviceType) {
    this.service = service;
    this.serviceType = serviceType;
  }

  public Object coerce(Object value, Type type) {
    if (value == null && type == serviceType) return service;
    return null;
  }
}
