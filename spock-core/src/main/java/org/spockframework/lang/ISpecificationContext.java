package org.spockframework.lang;

import org.spockframework.util.Beta;
import org.spockframework.mock.IMockController;
import org.spockframework.runtime.model.IterationInfo;

@Beta
public interface ISpecificationContext {
  IterationInfo getIterationInfo();

  Throwable getThrownException();

  IMockController getMockController();
}
