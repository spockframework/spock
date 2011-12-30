package org.spockframework.builder;

import java.util.*;

public class PropertiesConfigurationSource implements IConfigurationSource {
  private final Map<String, ?> properties;

  public PropertiesConfigurationSource(Map<String, ?> properties) {
    this.properties = properties;
  }

  public void configure(IConfigurationTarget target) {
    for (Map.Entry<String, ?> property: properties.entrySet()) {
      setProperty(target, property.getKey(), property.getValue());
    }
  }

  // IDEA: could use configureSlot instead of writeSlot and even instead of readSlot
  private void setProperty(IConfigurationTarget target, String path, Object value) {
    String[] pathParts = path.split("\\.");
    int numParts = pathParts.length;
    for (int i = 0; i < numParts - 1; i++) {
      target = target.readSlot(pathParts[i]);
    }
    target.writeSlot(pathParts[numParts - 1], value);
  }
}
