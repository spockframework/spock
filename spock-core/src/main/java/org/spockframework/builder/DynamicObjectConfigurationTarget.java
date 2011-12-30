package org.spockframework.builder;

import org.spockframework.util.GroovyRuntimeUtil;

import java.util.List;

public class DynamicObjectConfigurationTarget implements IConfigurationTarget {
  private final Object object;
  private final ITypeCoercer coercer;

  public DynamicObjectConfigurationTarget(Object object, ITypeCoercer coercer) {
    this.object = object;
    this.coercer = coercer;
  }

  public Object getSubject() {
    return object;
  }

  public IConfigurationTarget readSlot(String name) {
    Object value = GroovyRuntimeUtil.getProperty(object, name);
    IConfigurationValue configurationValue = new ConfigurationValue(value, value.getClass());
    return (IConfigurationTarget) coercer.coerce(configurationValue, IConfigurationTarget.class);
  }

  public void writeSlot(String name, Object value) {
    GroovyRuntimeUtil.setProperty(object, name, value);
  }

  public void configureSlot(String name, List<Object> values, IConfigurationSource source) {
    if (source != null) {
      IConfigurationTarget target = (IConfigurationTarget) coercer.coerce(values.get(0), IConfigurationTarget.class);
      source.configure(target);
    }
    GroovyRuntimeUtil.invokeMethod(object, name, values.toArray());
  }
}
