package spock.mock;

import java.util.Map;

import spock.lang.Beta;

@Beta
public class MockConfiguration {
  private final String name;
  private final Class<?> type;
  private final String nature;
  private final String impl;
  private final boolean global;

  public MockConfiguration(String name, Class<?> type,String nature, String impl, Map<String, Object> options) {
    this.name = name;
    this.type = type;
    this.nature = nature;
    this.impl = impl;
    this.global = options.containsKey("global") ? (Boolean) options.get("global") : false;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public String getNature() {
    return nature;
  }

  public String getImpl() {
    return impl;
  }

  public boolean isGlobal() {
    return global;
  }
}
