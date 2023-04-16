package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class AlphabeticalSpecOrderer extends SpecOrderer {
  private final boolean descending;

  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures, boolean descending) {
    super(orderSpecs, orderFeatures);
    this.descending = descending;
  }

  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this(orderSpecs, orderFeatures, false);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    AtomicInteger i = new AtomicInteger();
    specs.stream()
      .sorted((o1, o2) -> descending
        ? o2.getDisplayName().compareTo(o1.getDisplayName())
        : o1.getDisplayName().compareTo(o2.getDisplayName())
      )
      .forEach(specInfo -> specInfo.setExecutionOrder(i.getAndIncrement()));
  }

  @Override
  protected void orderFeatures(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs) {
      AtomicInteger i = new AtomicInteger();
      spec.getAllFeatures().stream()
        .sorted((o1, o2) -> descending
          ? o2.getDisplayName().compareTo(o1.getDisplayName())
          : o1.getDisplayName().compareTo(o2.getDisplayName())
        )
        .forEach(featureInfo -> featureInfo.setExecutionOrder(i.getAndIncrement()));
    }
  }

  public boolean isDescending() {
    return descending;
  }
}
