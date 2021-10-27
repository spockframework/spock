package org.spockframework.runtime;

import java.util.Iterator;
import java.util.List;

public interface DataIterator extends Iterator<Object[]>, AutoCloseable {
  int getEstimatedNumIterations();
  List<String> getDataVariableNames();
}
