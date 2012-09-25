package org.spockframework.mock;

import org.spockframework.util.Nullable;

import java.util.List;

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
