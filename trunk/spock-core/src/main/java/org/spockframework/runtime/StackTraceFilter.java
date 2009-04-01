/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Filters an exception's stack trace. Removes internal Groovy and Spock methods, and
 * restores the original names of feature methods (as present in source code).
 *
 * @author Peter Niederwieser
 */
public class StackTraceFilter {
  private static final List<String> FILTERED_PACKAGES = Arrays.asList(
      "org.spockframework.runtime",
      "org.codehaus.groovy.runtime.callsite",
      "java.lang.reflect",
      "sun.reflect"
  );

  private static final List<String> FILTERED_METHODS = Arrays.asList(
      "current",
      "call",
      "callCurrent",
      "callStatic"
  );
  
  private final IMethodNameMapper mapper;

  public StackTraceFilter(IMethodNameMapper mapper) {
    this.mapper = mapper;
  }

  public void filter(Throwable throwable) {
    List<StackTraceElement> result = new ArrayList<StackTraceElement>();

    for (StackTraceElement elem : throwable.getStackTrace()) {
      if (FILTERED_PACKAGES.contains(getPackageName(elem))) continue;
      if (isSyntheticMethod(elem)) continue;
      result.add(restoreFeatureMethodName(elem));
    }

    throwable.setStackTrace(result.toArray(new StackTraceElement[result.size()]));
  }

  private StackTraceElement restoreFeatureMethodName(StackTraceElement elem) {
    if (!elem.getMethodName().startsWith("__feature")) return elem;
    return new StackTraceElement(elem.getClassName(),
        mapper.map(elem.getMethodName()), elem.getFileName(), elem.getLineNumber());
  }

  private static String getPackageName(StackTraceElement elem) {
    int index = elem.getClassName().lastIndexOf('.');
    return elem.getClassName().substring(0, index);
  }

  private static boolean isSyntheticMethod(StackTraceElement elem) {
    return elem.getMethodName().contains("$")
        || elem.getClassName().contains("$")
            && FILTERED_METHODS.contains(elem.getMethodName());
  }
}
