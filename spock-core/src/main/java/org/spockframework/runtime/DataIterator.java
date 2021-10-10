package org.spockframework.runtime;

import java.util.Iterator;

public interface DataIterator extends Iterator<Object[]>, AutoCloseable {
  int getEstimatedNumIterations();
}
