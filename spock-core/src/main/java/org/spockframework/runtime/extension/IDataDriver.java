package org.spockframework.runtime.extension;

import org.spockframework.runtime.DataIterator;

public interface IDataDriver {
  IDataDriver DEFAULT = (dataIterator, iterationRunner) -> {
    while (dataIterator.hasNext()) {
      iterationRunner.runIteration(dataIterator.next());
    }
  };

  void runIterations(DataIterator dataIterator, IIterationRunner iterationRunner);
}
