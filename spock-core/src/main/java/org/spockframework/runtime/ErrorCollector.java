package org.spockframework.runtime;

import org.junit.runners.model.MultipleFailureException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ErrorCollector {
  private final boolean errorCollectionEnabled;

  private final List<Throwable> throwables = new CopyOnWriteArrayList<Throwable>();

  public ErrorCollector(boolean enabled) {
    errorCollectionEnabled = enabled;
  }


  public <T extends Throwable> void collectOrThrow(T error) throws T {
    if (errorCollectionEnabled){
      throwables.add(error);
    }else {
      throw error;
    }
  }

  public static final String VALIDATE_COLLECTED_ERRORS = "validateCollectedErrors";

  public void validateCollectedErrors() throws MultipleFailureException {
    if (errorCollectionEnabled){
      if (!throwables.isEmpty()){
        throw new MultipleFailureException(throwables);
      }
    }
  }
}
