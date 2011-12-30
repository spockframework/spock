package org.spockframework.builder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CoercerChain implements ITypeCoercer {
  private final List<ITypeCoercer> coercers = new ArrayList<ITypeCoercer>();

  public void add(ITypeCoercer coercer) {
    coercers.add(coercer);
  }

  public Object coerce(Object value, Type type) {
    for (ITypeCoercer coercer: coercers) {
      Object result = coercer.coerce(value, type);
      if (result != null) return result;
    }
    return null;
  }
}
