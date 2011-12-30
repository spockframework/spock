package org.spockframework.builder;

public class NotCoerceableException extends RuntimeException {
  private String msg;

  public NotCoerceableException(String msg) {
    this.msg = msg;
  }

  public NotCoerceableException withArgs(Object... args) {
    msg = String.format(msg, args);
    return this;
  }

  @Override
  public String getMessage() {
    return msg;
  }
}
