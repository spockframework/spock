package org.spockframework.builder;

import java.lang.reflect.Type;

public class PojoConfigurationTargetCoercer implements ITypeCoercer {
  private final ITypeCoercer coercer;

  public PojoConfigurationTargetCoercer(ITypeCoercer coercer) {
    this.coercer = coercer;
  }

  public Object coerce(Object value, Type type) {
    if (!(value instanceof IConfigurationValue)) return null;
    
    IConfigurationValue configurationValue = (IConfigurationValue) value;
    ISlotFinder slotFinder = (ISlotFinder) coercer.coerce(null, ISlotFinder.class);
    if (slotFinder == null) {
      throw new RuntimeException("PojoConfigurationTarget requires an ISlotFinder, but coercer doesn't provide one");
    }
    return new PojoModelTarget(configurationValue.getValue(), configurationValue.getType(), coercer, slotFinder);
  }
}
