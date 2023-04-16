package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Order;

import java.util.Collection;

/**
 * Spec orderer for usef-defined, manual specification and/or feature method ordering, to be used in connection with
 * {@link Order @Order} annotations. See the Spock user manual for more details.
 */
public class AnnotatationBasedSpecOrderer extends SpecOrderer {
  /**
   * Create an annotation-based spec orderer
   * @see Order
   */
  public AnnotatationBasedSpecOrderer() {
    super(true, true);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs) {
      Order orderAnnotation = spec.getAnnotation(Order.class);
      spec.setExecutionOrder(orderAnnotation == null ? 0 : orderAnnotation.value());
    }
  }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) {
    for (FeatureInfo feature : features) {
      Order orderAnnotation = feature.getFeatureMethod().getAnnotation(Order.class);
      feature.setExecutionOrder(orderAnnotation == null ? 0 : orderAnnotation.value());
    }
  }
}
