package org.spockframework.runtime;

public class ErrorRethrower extends ErrorCollector {
  public static final ErrorRethrower INSTANCE = new ErrorRethrower();

  private ErrorRethrower() {
    if (INSTANCE != null) {
      throw new UnsupportedOperationException();
    }
  }

  public <T extends Throwable> void collectOrThrow(T error) throws T {
    throw error;
  }

  @Override
  public void validateCollectedErrors() {
  }
}
