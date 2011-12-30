package org.spockframework.builder;

import java.lang.reflect.Type;

public interface IConfigurationValue {
  Object getValue();
  Type getType();
}
