package org.spockframework.builder;

import java.lang.reflect.Type;

public interface ITypeCoercer {
  Object coerce(Object value, Type type);
}
