package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class AlphabeticalSpecOrderer extends SpecOrderer {
  private final boolean ascending;

  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures, boolean ascending) {
    super(orderSpecs, orderFeatures);
    this.ascending = ascending;
  }

  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this(orderSpecs, orderFeatures, true);
  }

  public AlphabeticalSpecOrderer() {
    this(true, true);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    AtomicInteger i = new AtomicInteger();
    specs.stream()
      .sorted((o1, o2) -> ascending
        ? o1.getDisplayName().compareTo(o2.getDisplayName())
        : o2.getDisplayName().compareTo(o1.getDisplayName())
      )
      .forEach(specInfo -> specInfo.setExecutionOrder(i.getAndIncrement()));
  }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) {
    AtomicInteger i = new AtomicInteger();
    features.stream()
      .sorted((o1, o2) -> ascending
        ? o1.getDisplayName().compareTo(o2.getDisplayName())
        : o2.getDisplayName().compareTo(o1.getDisplayName())
      )
      .forEach(featureInfo -> featureInfo.setExecutionOrder(i.getAndIncrement()));
  }

  public boolean isAscending() {
    return ascending;
  }
}
