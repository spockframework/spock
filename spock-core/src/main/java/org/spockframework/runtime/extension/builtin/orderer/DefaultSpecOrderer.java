package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

public class DefaultSpecOrderer extends SpecOrderer {
  public DefaultSpecOrderer() {
    super(false, false);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) { }

  @Override
  protected void orderFeatures(Collection<SpecInfo> specs) { }
}
