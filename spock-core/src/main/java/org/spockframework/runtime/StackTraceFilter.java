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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spockframework.util.InternalIdentifiers;

/**
 * Filters an exception's stack trace. Removes internal Groovy and Spock methods, and
 * restores the original names of feature methods (as specified in source code).
 * Stack trace elements below a feature/fixture method invocation are truncated.
 *
 * @author Peter Niederwieser
 */
// IDEA: do not filter top-most stack element, unless it's a call to the Spock runtime
// IDEA: find entry points into user code by looking for BaseSpecRunner.invoke()/invokeRaw()
public class StackTraceFilter implements IStackTraceFilter {
  private static final Pattern FILTERED_CLASSES = Pattern.compile(
      "org.codehaus.groovy.runtime\\..*" + 
      "|org.codehaus.groovy.reflection\\..*" +
      "|org.codehaus.groovy\\..*MetaClass.*" +
      "|groovy\\..*MetaClass.*" +
      "|groovy.lang.MetaMethod" +
      "|java.lang.reflect\\..*" +
      "|sun.reflect\\..*" +
      "|org.spockframework.runtime\\.[^\\.]+" // exclude subpackages
  );

  private static final Pattern CLOSURE_CLASS = Pattern.compile("(.+)\\$_(.+)_closure(\\d+)");

  private final IMethodNameMapper mapper;

  public StackTraceFilter(IMethodNameMapper mapper) {
    this.mapper = mapper;
  }

  public void filter(Throwable throwable) {
    List<StackTraceElement> filteredTrace = new ArrayList<StackTraceElement>();

    for (StackTraceElement elem : throwable.getStackTrace()) {
      if (isInitializerOrFixtureMethod(elem)) {
        filteredTrace.add(elem);
        break;
      }
      if (checkForAndAddPrettyPrintedFeatureMethod(elem, filteredTrace)) break;
      if (isFilteredClass(elem)) continue;
      if (checkForAndAddPrettyPrintedClosureInvocation(elem, filteredTrace)) continue;
      if (isGeneratedMethod(elem)) continue;
      filteredTrace.add(elem);
    }

    throwable.setStackTrace(filteredTrace.toArray(new StackTraceElement[filteredTrace.size()]));

    if (throwable.getCause() != null) filter(throwable.getCause());
  }

  private boolean isInitializerOrFixtureMethod(StackTraceElement elem) {
    return mapper.isInitializerOrFixtureMethod(elem.getClassName(), elem.getMethodName());
  }

  private static boolean isFilteredClass(StackTraceElement elem) {
    return FILTERED_CLASSES.matcher(elem.getClassName()).matches();
  }

  private boolean checkForAndAddPrettyPrintedFeatureMethod(StackTraceElement elem, List<StackTraceElement> trace) {
    if (!InternalIdentifiers.isFeatureMethodName(elem.getMethodName())) return false;
    trace.add(prettyPrintFeatureMethod(elem));
    return true;
  }

  private StackTraceElement prettyPrintFeatureMethod(StackTraceElement elem) {
    return new StackTraceElement(elem.getClassName(),
        mapper.toFeatureName(elem.getMethodName()), elem.getFileName(), elem.getLineNumber());
  }

  private boolean checkForAndAddPrettyPrintedClosureInvocation(StackTraceElement elem, List<StackTraceElement> trace) {
    if (!elem.getMethodName().equals("doCall")) return false;
    Matcher matcher = CLOSURE_CLASS.matcher(elem.getClassName());
    if (!matcher.matches()) return false;

    trace.add(prettyPrintClosureInvocation(elem, matcher));
    return true;
  }

  private StackTraceElement prettyPrintClosureInvocation(StackTraceElement elem, Matcher matcher) {
    String classContainingClosureDef = matcher.group(1);
    String methodContainingClosureDef = matcher.group(2);
    String consecutiveNumberOfClosureDef = matcher.group(3);

    String prettyClassName = classContainingClosureDef;
    String prettyMethodName =
        mapper.toFeatureName(methodContainingClosureDef) + "_closure" + consecutiveNumberOfClosureDef;

    return new StackTraceElement(prettyClassName, prettyMethodName, elem.getFileName(), elem.getLineNumber());
  }

  private boolean isGeneratedMethod(StackTraceElement elem) {
    return elem.getLineNumber() < 0;
  }
}