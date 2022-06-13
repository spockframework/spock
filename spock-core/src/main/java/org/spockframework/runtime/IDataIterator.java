package org.spockframework.runtime;

import org.spockframework.util.Beta;

import java.util.*;

/**
 * A special iterator, that gives to the data produced by Spock's data providers.
 *
 * @author Leonard Brünings
 * @since 2.2
 */
@Beta
public interface IDataIterator extends Iterator<Object[]>, AutoCloseable {
  /**
   * @return the number of data sets that are provided by this iterator. This will be {@code -1} if it cannot be determined.
   */
  int getEstimatedNumIterations();

  /**
   * @return the names of the data variables in the order they are present in the array
   */
  List<String> getDataVariableNames();
}
