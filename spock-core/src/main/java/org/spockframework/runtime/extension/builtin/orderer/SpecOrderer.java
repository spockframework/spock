package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.SpecProcessor;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

public abstract class SpecOrderer implements SpecProcessor {
  protected final boolean orderSpecs;
  protected final boolean orderFeatures;

  public SpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this.orderSpecs = orderSpecs;
    this.orderFeatures = orderFeatures;
  }

  @Override
  public void process(Collection<SpecInfo> specs) {
    if (orderSpecs)
      orderSpecs(specs);
    if (orderFeatures)
      orderFeatures(specs);
  }

  protected abstract void orderSpecs(Collection<SpecInfo> specs);
  protected abstract void orderFeatures(Collection<SpecInfo> specs);

  public boolean isOrderSpecs() {
    return orderSpecs;
  }

  public boolean isOrderFeatures() {
    return orderFeatures;
  }
}
