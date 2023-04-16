package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

/**
 * No-op spec orderer, used as a default if no other orderer is configured
 */
public class DefaultSpecOrderer extends SpecOrderer {
  /**
   * Create a no-op spec orderer
   */
  public DefaultSpecOrderer() {
    super(false, false);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) { }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) { }
}
