package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Order;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotatationBasedSpecOrderer extends SpecOrderer {
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
  protected void orderFeatures(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs) {
      for (FeatureInfo feature : spec.getAllFeatures()) {
        Order orderAnnotation = feature.getFeatureMethod().getAnnotation(Order.class);
        feature.setExecutionOrder(orderAnnotation == null ? 0 : orderAnnotation.value());
      }
    }
  }
}
