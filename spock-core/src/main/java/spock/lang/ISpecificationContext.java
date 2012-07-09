package spock.lang;

import org.spockframework.mock.IMockController;
import org.spockframework.runtime.model.IterationInfo;

@Beta
public interface ISpecificationContext {
  IterationInfo getIterationInfo();

  Throwable getThrownException();

  IMockController getMockController();
}
