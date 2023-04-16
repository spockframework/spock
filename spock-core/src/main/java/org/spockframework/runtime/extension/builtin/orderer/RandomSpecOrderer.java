package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomSpecOrderer extends SpecOrderer {
  private final Random random;

  public RandomSpecOrderer(boolean orderSpecs, boolean orderFeatures, long seed) {
    super(orderSpecs, orderFeatures);
    random = new Random(seed);
  }

  public RandomSpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this(orderSpecs, orderFeatures, System.currentTimeMillis());
  }

  public RandomSpecOrderer() {
    this(true, true);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs)
      spec.setExecutionOrder(random.nextInt());
  }

  @Override
  protected void orderFeatures(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs) {
      for (FeatureInfo feature : spec.getAllFeatures())
        feature.setExecutionOrder(random.nextInt());
    }
  }
}
