package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orderer capable of assigning specification and/or feature method run orders according to spec/feature display names,
 * comparing them alphabetically. There is no locale-specific collation, only simple string comparison based on the
 * default JVM locale, using by {@link String#compareTo(String)}.
 */
public class AlphabeticalSpecOrderer extends SpecOrderer {
  private final boolean ascending;

  /**
   * Create an alphabetical spec orderer
   *
   * @param orderSpecs    modify specification run order (yes/no)?
   * @param orderFeatures modify feature run order within a specification (yes/no)?
   * @param ascending     sort in ascending order (yes/no)?
   */
  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures, boolean ascending) {
    super(orderSpecs, orderFeatures);
    this.ascending = ascending;
  }

  /**
   * Create an alphabetical spec orderer with a default ascending sort order
   *
   * @param orderSpecs    modify specification run order (yes/no)?
   * @param orderFeatures modify feature run order within a specification (yes/no)?
   * @see #AlphabeticalSpecOrderer(boolean, boolean, boolean)
   */
  public AlphabeticalSpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this(orderSpecs, orderFeatures, true);
  }

  /**
   * Create an alphabetical spec orderer with default values. This is a shorthand for calling
   * {@link #AlphabeticalSpecOrderer(boolean, boolean, boolean)} with parameters {@code true, true, true}.
   */
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
