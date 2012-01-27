package org.spockframework.builder;

import org.spockframework.util.GroovyRuntimeUtil;

import java.util.List;

public class DynamicObjectModelTarget implements IModelTarget {
  private final Object object;
  private final ITypeCoercer coercer;

  public DynamicObjectModelTarget(Object object, ITypeCoercer coercer) {
    this.object = object;
    this.coercer = coercer;
  }

  public Object getSubject() {
    return object;
  }

  public IModelTarget readSlot(String name) {
    Object value = GroovyRuntimeUtil.getProperty(object, name);
    IConfigurationValue configurationValue = new ConfigurationValue(value, value.getClass());
    return (IModelTarget) coercer.coerce(configurationValue, IModelTarget.class);
  }

  public void writeSlot(String name, Object value) {
    GroovyRuntimeUtil.setProperty(object, name, value);
  }

  public void configureSlot(String name, List<Object> values, IModelSource source) {
    if (source != null) {
      IModelTarget target = (IModelTarget) coercer.coerce(values.get(0), IModelTarget.class);
      source.configure(target);
    }
    GroovyRuntimeUtil.invokeMethod(object, name, values.toArray());
  }
}
