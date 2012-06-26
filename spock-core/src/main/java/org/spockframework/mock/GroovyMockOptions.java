package org.spockframework.mock;

import java.util.Map;

import org.spockframework.util.GroovyRuntimeUtil;

public class GroovyMockOptions {
  private boolean forceCglib;

  private boolean dynamicMembers;

  private boolean staticMembers;

  private boolean globalEffect;

  private boolean partial;

  public boolean isForceCglib() {
    return forceCglib;
  }

  public void setForceCglib(boolean forceCglib) {
    this.forceCglib = forceCglib;
  }

  public boolean isDynamicMembers() {
    return dynamicMembers;
  }

  public void setDynamicMembers(boolean dynamicMembers) {
    this.dynamicMembers = dynamicMembers;
  }

  public boolean isStaticMembers() {
    return staticMembers;
  }

  public void setStaticMembers(boolean staticMembers) {
    this.staticMembers = staticMembers;
  }

  public boolean isGlobalEffect() {
    return globalEffect;
  }

  public void setGlobalEffect(boolean globalEffect) {
    this.globalEffect = globalEffect;
  }

  public boolean isPartial() {
    return partial;
  }

  public void setPartial(boolean partial) {
    this.partial = partial;
  }

  public static GroovyMockOptions parse(Map<String, Object> map) {
    GroovyMockOptions options = new GroovyMockOptions();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      GroovyRuntimeUtil.setProperty(options, entry.getKey(), entry.getValue());
    }
    return options;
  }
}
