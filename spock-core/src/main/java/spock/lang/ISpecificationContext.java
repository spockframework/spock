package spock.lang;

import org.spockframework.mock.IMockInvocationMatcher;
import org.spockframework.runtime.model.IterationInfo;

@Beta
public interface ISpecificationContext {
  IterationInfo getIterationInfo();

  Throwable getThrownException();

  IMockInvocationMatcher getMockInvocationMatcher();
}
