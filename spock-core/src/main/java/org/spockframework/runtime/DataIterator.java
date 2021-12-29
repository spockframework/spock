package org.spockframework.runtime;

import java.util.Iterator;
import java.util.List;

/**
 * A special iterator, that gives to the data produced by Spock's data providers.
 *
 * @since 2.2
 * @author Leonard Brünings
 */
public interface DataIterator extends Iterator<Object[]>, AutoCloseable {
  /**
   * @return the number of data sets that are provided by this iterator. This will be {@code -1} if it cannot be determined.
   */
  int getEstimatedNumIterations();

  /**
   * @return the names of the data variables in the order they are present in the array
   */
  List<String> getDataVariableNames();
}
