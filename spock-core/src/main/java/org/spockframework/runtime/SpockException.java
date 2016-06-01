package org.spockframework.runtime;

public class SpockException extends RuntimeException {
  public SpockException() {
    super();
  }

  public SpockException(String message) {
    super(message);
  }

  public SpockException(String message, Throwable cause) {
    super(message, cause);
  }

  public SpockException(Throwable cause) {
    super(cause);
  }
}
