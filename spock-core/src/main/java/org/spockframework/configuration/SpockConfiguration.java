package org.spockframework.configuration;

import org.spockframework.builder.DelegatingScript;

import java.util.List;

public class SpockConfiguration {
  private final List<Object> configSources;

  public SpockConfiguration(List<Object> configSources) {
    this.configSources = configSources;
  }

  public <T> T getConfiguration(Class<T> configurationType) {
    return null;
  }
}
