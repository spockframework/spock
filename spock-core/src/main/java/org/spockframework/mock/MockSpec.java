package org.spockframework.mock;

import java.util.Collections;
import java.util.Map;

public class MockSpec {
  private final String name;
  private final Class<?> type;
  private final String kind;
  private final Map<String, Object> options;

  public MockSpec(String name, Class<?> type, String kind) {
    this(name, type, kind, Collections.<String, Object>emptyMap());
  }

  public MockSpec(String name, Class<?> type, String kind, Map<String, Object> options) {
    this.name = name;
    this.type = type;
    this.kind = kind;
    this.options = options;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public String getKind() {
    return kind;
  }

  public Map<String, Object> getOptions() {
    return options;
  }
}
