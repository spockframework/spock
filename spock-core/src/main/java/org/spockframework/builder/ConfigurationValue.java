package org.spockframework.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ConfigurationValue implements IConfigurationValue {
  private final Object value;
  private final Type type;
  private List<Annotation> annotations;

  public ConfigurationValue(Object value) {
    this(value, value.getClass());
  }

  public ConfigurationValue(Object value, Type type) {
    this(value, type, Collections.<Annotation>emptyList());
  }

  public ConfigurationValue(Object value, Type type, List<Annotation> annotations) {
    this.value = value;
    this.type = type;
    this.annotations = annotations;
  }

  public Object getValue() {
    return value;
  }

  public Type getType() {
    return type;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }
}
