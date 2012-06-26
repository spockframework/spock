package org.spockframework.lang;

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
}
