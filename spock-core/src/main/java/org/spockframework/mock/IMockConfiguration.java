package org.spockframework.mock;

import java.util.List;

import org.spockframework.util.Nullable;

public interface IMockConfiguration {
  @Nullable
  String getName();

  Class<?> getType();

  MockNature getNature();

  MockImplementation getImplementation();

  @Nullable
  List<Object> getConstructorArgs();

  IDefaultResponse getDefaultResponse();

  boolean isGlobal();

  boolean isVerified();

  boolean isUseObjenesis();
}
