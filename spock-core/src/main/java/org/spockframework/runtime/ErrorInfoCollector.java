package org.spockframework.runtime;

import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.util.ExceptionUtil;

import java.util.*;
import java.util.stream.Collectors;

import org.opentest4j.MultipleFailuresError;

public class ErrorInfoCollector {
  private List<ErrorInfo> errorInfos = new ArrayList<>();

  public void addErrorInfo(ErrorInfo errorInfo) {
    errorInfos.add(errorInfo);
  }

  public boolean isEmpty() {
    return errorInfos.isEmpty();
  }

  public boolean hasErrors() {
    return !errorInfos.isEmpty();
  }

  public void assertEmpty() {
    if (!errorInfos.isEmpty()) {
      if (errorInfos.size() == 1) {
        ExceptionUtil.sneakyThrow(errorInfos.get(0).getException());
      }
      throw new SpockMultipleFailuresError("",
        errorInfos.stream().map(ErrorInfo::getException).collect(Collectors.toList()));
    }
  }
}
