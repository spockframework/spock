package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;
import java.util.Random;

/**
 * Orderer capable of randomizing specification and/or feature method run order
 */
public class RandomSpecOrderer extends SpecOrderer {
  private final Random random;

  /**
   * Create a random spec orderer
   *
   * @param orderSpecs    modify specification run order (yes/no)?
   * @param orderFeatures modify feature run order within a specification (yes/no)?
   * @param seed          random seed to be used in {@link Random#Random(long)}; setting this to a fixed value enables
   *                      you to create test runs with repeatable pseudo-random run ordering, which can be helpful when
   *                      e.g. debugging tests failing due to a particular run order, making them more independent of
   *                      each other in the process
   */
  public RandomSpecOrderer(boolean orderSpecs, boolean orderFeatures, long seed) {
    super(orderSpecs, orderFeatures);
    random = new Random(seed);
  }

  /**
   * Create a random spec orderer with a default random seed of {@code System.currentTimeMillis()}
   *
   * @param orderSpecs    modify specification run order (yes/no)?
   * @param orderFeatures modify feature run order within a specification (yes/no)?
   * @see #RandomSpecOrderer(boolean, boolean, long)
   */
  public RandomSpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this(orderSpecs, orderFeatures, System.currentTimeMillis());
  }

  /**
   * Create a random spec orderer with default values. This is a shorthand for calling
   * {@link #RandomSpecOrderer(boolean, boolean, long)} with parameters {@code true, true, System.currentTimeMillis()}.
   */
  public RandomSpecOrderer() {
    this(true, true);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs)
      spec.setExecutionOrder(random.nextInt());
  }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) {
    for (FeatureInfo feature : features)
      feature.setExecutionOrder(random.nextInt());
  }
}
