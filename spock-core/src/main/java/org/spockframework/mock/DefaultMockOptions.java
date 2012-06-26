package org.spockframework.mock;

import java.util.Map;

import org.spockframework.util.GroovyRuntimeUtil;

public class DefaultMockOptions {
  private boolean forceCglib;

  public boolean isForceCglib() {
    return forceCglib;
  }

  public void setForceCglib(boolean forceCglib) {
    this.forceCglib = forceCglib;
  }

  public static DefaultMockOptions parse(Map<String, Object> map) {
    DefaultMockOptions options = new DefaultMockOptions();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      GroovyRuntimeUtil.setProperty(options, entry.getKey(), entry.getValue());
    }
    return options;
  }
}
