package org.spockframework.runtime.extension;

import org.spockframework.runtime.DataIterator;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.ParameterInfo;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;
import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

/**
 * The data driver is responsible to map the data from the data providers to the individual iterations.
 * <p>
 * It can be used to change the behavior of the data provider, e.g. to provide data in a different order,
 * filter data iterations, produce more iterations.
 * <p>
 * A thing to keep in mind is that if the order is not consistent between runs,
 * then it will interfere with {@link org.junit.platform.engine.discovery.DiscoverySelectors.IterationSelector}.
 *
 * @since 2.2
 * @author Leonard Brünings
 */
public interface IDataDriver {
  /**
   * The default implementation of IDataDriver.
   * <p>
   * It simply runs all iterations.
   */
  IDataDriver DEFAULT = (dataIterator, iterationRunner, parameters) -> {
    while (dataIterator.hasNext()) {
      iterationRunner.runIteration(dataIterator.next());
    }
  };

  /**
   * Run all iterations of the test method.
   * <p>
   * A custom implementation of the DataDriver can choose to run fewer or more iterations than the data iterator provides.
   *
   * @param dataIterator the data iterator giving access to the data from the data providers.
   * @param iterationRunner the iteration runner that will be used to run the test method for each iteration.
   * @param parameters the parameters of the test method
   */
  void runIterations(DataIterator dataIterator, IIterationRunner iterationRunner, List<ParameterInfo> parameters);
}
