package org.spockframework.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import org.spockframework.builder.DelegatingScript;

import spock.config.ConfigurationException;

public class SpockConfigurationBuilder {
  private final List<Object> configSources = new ArrayList<Object>();
  
  public SpockConfigurationBuilder fromScript(String scriptText) {
    configSources.add(scriptText);
    return this;
  }
  
  public SpockConfigurationBuilder fromProperties(Map<String, Object> properties) {
    configSources.add(properties);
    return this;
  }
  
  public SpockConfiguration build() {
    return new SpockConfiguration(configSources);
  }
}
