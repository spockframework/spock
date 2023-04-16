package org.spockframework.runtime;

import org.spockframework.runtime.extension.builtin.orderer.SpecOrderer;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

/**
 * Generic bulk processor for a collection of {@link SpecInfo} elements
 *
 * @see SpecOrderer
 */
public interface SpecProcessor {
  /**
   * Bulk-process a collection of {@link SpecInfo} elements in-place, i.e. do not return anything but operate on the
   * elements given, changing their state if necessary.
   *
   * @param specs spec-info instances to be processed
   */
  void process(Collection<SpecInfo> specs);
}
