/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime;

import org.spockframework.util.InternalIdentifiers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.newSetFromMap;

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
      "\\Qorg.codehaus.groovy.runtime.\\E.*" +
      "|\\Qgroovy.lang.Closure\\E" +
      "|\\Qorg.codehaus.groovy.reflection.\\E.*" +
      "|\\Qorg.codehaus.groovy.\\E.*MetaClass.*" +
      "|\\Qorg.codehaus.groovy.vmplugin.v8.\\E.*" +
      "|groovy\\..*MetaClass.*" +
      "|groovy\\.lang\\.MetaMethod" +
      "|\\Qjava.lang.invoke.\\E.*" +
      "|\\Qjava.lang.reflect.\\E.*" +
      "|sun\\.reflect\\..*" +
      "|\\Qjdk.internal.reflect.\\E.*" +
      "|\\Qorg.spockframework.runtime.\\E[^.]+" // exclude subpackages
  );

  private static final Pattern CLOSURE_CLASS = Pattern.compile("(.+)\\$_(.+)_closure(\\d+)");
  private static final StackTraceElement[] STACK_TRACE_ELEMENTS = new StackTraceElement[0];

  private final IMethodNameMapper mapper;

  public StackTraceFilter(IMethodNameMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void filter(Throwable throwable) {
    filter(throwable, newSetFromMap(new IdentityHashMap<>()));
  }

  private void filter(Throwable throwable, Set<Throwable> seen) {
    if (!seen.add(throwable)) {
      return;
    }

    List<StackTraceElement> filteredTrace = new ArrayList<>();

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

    throwable.setStackTrace(filteredTrace.toArray(STACK_TRACE_ELEMENTS));

    if (throwable.getCause() != null) filter(throwable.getCause(), seen);

    for (Throwable suppressed : throwable.getSuppressed()) {
      filter(suppressed, seen);
    }
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
    if (!"doCall".equals(elem.getMethodName())) return false;
    Matcher matcher = CLOSURE_CLASS.matcher(elem.getClassName());
    if (!matcher.matches()) return false;

    trace.add(prettyPrintClosureInvocation(elem, matcher));
    return true;
  }

  private StackTraceElement prettyPrintClosureInvocation(StackTraceElement elem, Matcher matcher) {
    String classContainingClosureDef = matcher.group(1);
    String methodContainingClosureDef = matcher.group(2);
    String consecutiveNumberOfClosureDef = matcher.group(3);

    // Compensate for Groovy 2.4+'s mangling of closure class names, which turns `$spock_` into `_spock_`
    String unmangledName = "$" + methodContainingClosureDef.substring(1);
    if (InternalIdentifiers.isInternalName(unmangledName)) {
      methodContainingClosureDef = unmangledName;
    }

    String prettyClassName = classContainingClosureDef;
    String prettyMethodName =
        mapper.toFeatureName(methodContainingClosureDef) + "_closure" + consecutiveNumberOfClosureDef;

    return new StackTraceElement(prettyClassName, prettyMethodName, elem.getFileName(), elem.getLineNumber());
  }

  private boolean isGeneratedMethod(StackTraceElement elem) {
    return elem.getLineNumber() < 0;
  }
}
