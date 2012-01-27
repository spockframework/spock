package org.spockframework.builder;

import java.util.*;

public class PropertiesModelSource implements IModelSource {
  private final Map<String, ?> properties;

  public PropertiesModelSource(Map<String, ?> properties) {
    this.properties = properties;
  }

  public void configure(IModelTarget target) {
    for (Map.Entry<String, ?> property: properties.entrySet()) {
      setProperty(target, property.getKey(), property.getValue());
    }
  }

  // IDEA: could use configureSlot instead of writeSlot and even instead of readSlot
  private void setProperty(IModelTarget target, String path, Object value) {
    String[] pathParts = path.split("\\.");
    int numParts = pathParts.length;
    for (int i = 0; i < numParts - 1; i++) {
      target = target.readSlot(pathParts[i]);
    }
    target.writeSlot(pathParts[numParts - 1], value);
  }
}
