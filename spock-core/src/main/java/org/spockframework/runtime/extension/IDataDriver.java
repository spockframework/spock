package org.spockframework.runtime.extension;

import org.spockframework.runtime.DataIterator;
import org.spockframework.runtime.model.ParameterInfo;

import java.util.List;

public interface IDataDriver {
  IDataDriver DEFAULT = (dataIterator, iterationRunner, parameters) -> {
    while (dataIterator.hasNext()) {
      iterationRunner.runIteration(dataIterator.next());
    }
  };

  void runIterations(DataIterator dataIterator, IIterationRunner iterationRunner, List<ParameterInfo> parameters);
}
