package org.spockframework.builder;

import groovy.lang.Closure;
import org.spockframework.gentyref.GenericTypeReflector;

import java.lang.reflect.Type;

public class ClosureConfigurationSourceCoercer implements ITypeCoercer {
  public Object coerce(Object value, Type type) {
    if (value instanceof Closure && GenericTypeReflector.erase(type).isAssignableFrom(ClosureConfigurationSource.class)) {
      return new ClosureConfigurationSource((Closure) value);
    }
    return null;
  }
}
