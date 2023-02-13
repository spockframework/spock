package org.spockframework.runtime.extension;

import static java.lang.System.arraycopy;
import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

import org.spockframework.runtime.IDataIterator;
import org.spockframework.runtime.model.*;
import org.spockframework.util.Beta;

import java.util.*;

/**
 * The data driver is responsible to map the data from the data providers to the individual iterations.
 * <p>
 * It can be used to change the behavior of the data provider, e.g. to provide data in a different order,
 * filter data iterations, produce more iterations.
 * <p>
 * A thing to keep in mind is that if the order is not consistent between runs,
 * then it will interfere with {@link org.junit.platform.engine.discovery.IterationSelector}.
 *
 * @author Leonard Brünings
 * @since 2.2
 */
@Beta
public interface IDataDriver {
  /**
   * The default implementation of IDataDriver.
   * <p>
   * It simply runs all iterations.
   */
  IDataDriver DEFAULT = (dataIterator, iterationRunner, parameters) -> {
    int estimatedNumIterations = dataIterator.getEstimatedNumIterations();
    while (dataIterator.hasNext()) {
      Object[] arguments = dataIterator.next();

      // dataIterator.next() will return null if an error occurs, so skip the iteration if it is null.
      if (arguments != null) {
        iterationRunner.runIteration(prepareArgumentArray(arguments, parameters), estimatedNumIterations);
      }
    }
  };

  /**
   * Run all iterations of the test method.
   * <p>
   * A custom implementation of the DataDriver can choose to run fewer or more iterations than the data iterator provides.
   * <p>
   * An implementation doesn't have to wait on the futures returned by the {@code iterationRunner},
   * the future can be used to make subsequent iterations depend on the outcome of previous iterations.
   *
   * @param dataIterator the data iterator giving access to the data from the data providers. The data iterator is not to be closed by this method.
   * @param iterationRunner the iteration runner that will be used to run the test method for each iteration.
   * @param parameters the parameters of the test method
   */
  void runIterations(IDataIterator dataIterator, IIterationRunner iterationRunner, List<ParameterInfo> parameters);

  /**
   * Prepares the arguments for invocation of the test method.
   * <p>
   * It is possible to have fewer arguments produced by the data driver than the number of parameters.
   * In this case, the missing arguments are filled with {@link MethodInfo#MISSING_ARGUMENT}.
   * <p>
   * Custom implementations of IDataDriver should use this method to prepare the argument array.
   * <p>
   * Important: The method relies on the fact that the data iterator produces the data in the same order as the parameters,
   * with missing arguments being on the end. If a custom implementation of IDataDriver does not follow this convention,
   * then it should not rely on this method. However, it must still follow the contract of setting the missing arguments as
   * {@link MethodInfo#MISSING_ARGUMENT}.
   *
   * @param arguments the arguments created by the data driver
   * @param parameters the parameters of the test method
   * @return an array of arguments that can be passed to the test method
   */
  static Object[] prepareArgumentArray(Object[] arguments, List<ParameterInfo> parameters) {
    int parameterCount = parameters.size();
    if (arguments.length == parameterCount) {
      return arguments;
    }

    Object[] methodArguments = new Object[parameterCount];
    arraycopy(arguments, 0, methodArguments, 0, arguments.length);
    Arrays.fill(methodArguments, arguments.length, parameterCount, MISSING_ARGUMENT);
    return methodArguments;
  }
}
