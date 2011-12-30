package org.spockframework.builder;

public class InvalidConfigurationException extends RuntimeException {
  private String msg;

  public InvalidConfigurationException(String msg) {
    this(msg, null);
  }

  public InvalidConfigurationException(String msg, Throwable throwable) {
    super(throwable);
    this.msg = msg;
  }

  public InvalidConfigurationException withArgs(Object... args) {
    msg = String.format(msg, args);
    return this;
  }

  @Override
  public String getMessage() {
    return msg;
  }
}
