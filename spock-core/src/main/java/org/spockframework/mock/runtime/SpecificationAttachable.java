package org.spockframework.mock.runtime;

import spock.lang.Specification;

/**
 * The Object implementing this interface can be attached to and
 * detached from a {@link Specification}.
 *
 * @author Leonard Bruenings
 */
public interface SpecificationAttachable {

  /**
   * Attaches the mock to a specification.
   *
   * @param specification specification that this mock object should attached to
   */
  void attach(Specification specification);

  /**
   * Detaches the mock from its current specification.
   */
  void detach();

}
