package org.spockframework.runtime;

import org.opentest4j.MultipleFailuresError;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ErrorCollector {
  private final List<Throwable> throwables = new CopyOnWriteArrayList<>();

  public <T extends Throwable> void collectOrThrow(T error) throws T {
    throwables.add(error);
  }

  public static final String VALIDATE_COLLECTED_ERRORS = "validateCollectedErrors";

  public void validateCollectedErrors() throws Throwable {
    switch (throwables.size()) {
      case 0:
        return;

      case 1:
        throw throwables.get(0);

      default:
        throw new MultipleFailuresError("", throwables);
    }
  }
}
