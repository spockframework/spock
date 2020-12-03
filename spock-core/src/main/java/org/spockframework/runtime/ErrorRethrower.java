package org.spockframework.runtime;

public class ErrorRethrower extends ErrorCollector {
  public <T extends Throwable> void collectOrThrow(T error) throws T {
    throw error;
  }

  @Override
  public void validateCollectedErrors() {
  }
}
