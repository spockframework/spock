/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import org.spockframework.lang.*;
import org.spockframework.mock.*;
import org.spockframework.mock.constraint.*;
import org.spockframework.mock.response.*;
import org.spockframework.runtime.InvalidSpecException;

import java.util.*;

import groovy.lang.Closure;

/**
 *
 * @author Peter Niederwieser
 */
public class InteractionBuilder {
  private final int line;
  private final int column;
  private final String text;

  private int minCount = 0;
  private int maxCount = Integer.MAX_VALUE;
  private List<IInvocationConstraint> invConstraints = new ArrayList<>();
  private List<Object> argNames;
  private List<IArgumentConstraint> argConstraints;
  private ResponseGeneratorChain responseGeneratorChain = new ResponseGeneratorChain();

  public InteractionBuilder(int line, int column, String text) {
    this.line = line;
    this.column = column;
    this.text = text;
  }

  public static final String SET_FIXED_COUNT = "setFixedCount";
  public InteractionBuilder setFixedCount(Object count) {
    if (count instanceof Wildcard) {
      minCount = 0;
      maxCount = Integer.MAX_VALUE;
    } else
      minCount = maxCount = convertCount(count, true);

    return this;
  }

  public static final String SET_RANGE_COUNT = "setRangeCount";
  public InteractionBuilder setRangeCount(Object minCount, Object maxCount, boolean inclusive) {
    this.minCount = minCount instanceof Wildcard ? 0 : convertCount(minCount, true);
    this.maxCount = maxCount instanceof Wildcard ? Integer.MAX_VALUE : convertCount(maxCount, inclusive);
    if (this.minCount > this.maxCount)
      throw new InvalidSpecException("lower bound of invocation count must come before upper bound");
    return this;
  }

  public static final String ADD_EQUAL_TARGET = "addEqualTarget";
  public InteractionBuilder addEqualTarget(Object target) {
    invConstraints.add(new TargetConstraint(target));
    return this;
  }

  public static final String ADD_WILDCARD_TARGET = "addWildcardTarget";
  public InteractionBuilder addWildcardTarget() {
    invConstraints.add(new TargetConstraint(Wildcard.INSTANCE));
    return this;
  }

  public static final String ADD_EQUAL_PROPERTY_NAME = "addEqualPropertyName";
  public InteractionBuilder addEqualPropertyName(String name) {
    if (name.equals(Wildcard.INSTANCE.toString())) {
      invConstraints.add(WildcardMethodNameConstraint.INSTANCE);
    } else {
      invConstraints.add(new EqualPropertyNameConstraint(name));
    }
    return this;
  }

  public static final String ADD_REGEX_PROPERTY_NAME = "addRegexPropertyName";
  public InteractionBuilder addRegexPropertyName(String regex) {
    invConstraints.add(new RegexPropertyNameConstraint(regex));
    return this;
  }

  public static final String ADD_EQUAL_METHOD_NAME = "addEqualMethodName";
  public InteractionBuilder addEqualMethodName(String name) {
    if (name.equals(Wildcard.INSTANCE.toString())) {
      invConstraints.add(WildcardMethodNameConstraint.INSTANCE);
    } else {
      invConstraints.add(new EqualMethodNameConstraint(name));
    }
    return this;
  }

  public static final String ADD_REGEX_METHOD_NAME = "addRegexMethodName";
  public InteractionBuilder addRegexMethodName(String regex) {
    invConstraints.add(new RegexMethodNameConstraint(regex));
    return this;
  }

  public static final String SET_ARG_LIST_KIND = "setArgListKind";
  public InteractionBuilder setArgListKind(boolean isPositional) {
    return setArgListKind(isPositional, false);
  }

  public InteractionBuilder setArgListKind(boolean isPositional, boolean isMixed) {
    argConstraints = new ArrayList<>();
    argNames = new ArrayList<>();
    if (isPositional) {
      invConstraints.add(new PositionalArgumentListConstraint(argConstraints, isMixed));
    } else {
      invConstraints.add(new NamedArgumentListConstraint(argNames, argConstraints, isMixed));
    }
    return this;
  }

  public static final String ADD_ARG_NAME= "addArgName";
  public InteractionBuilder addArgName(String name) {
    argNames.add(name);
    return this;
  }

  public static final String ADD_CODE_ARG = "addCodeArg";
  public InteractionBuilder addCodeArg(Closure closure) {
    argConstraints.add(new CodeArgumentConstraint(closure));
    return this;
  }

  public static final String ADD_EQUAL_ARG = "addEqualArg";
  public InteractionBuilder addEqualArg(Object arg) {
    argConstraints.add(arg instanceof Wildcard ? WildcardArgumentConstraint.INSTANCE :
        arg instanceof SpreadWildcard ? SpreadWildcardArgumentConstraint.INSTANCE :
            new EqualArgumentConstraint(arg));
    return this;
  }

  public static final String TYPE_LAST_ARG = "typeLastArg";
  public InteractionBuilder typeLastArg(Class<?> type) {
    IArgumentConstraint last = argConstraints.get(argConstraints.size() - 1);
    argConstraints.set(argConstraints.size() - 1, new TypeArgumentConstraint(type, last));
    return this;
  }

  public static final String NEGATE_LAST_ARG = "negateLastArg";
  public InteractionBuilder negateLastArg() {
    IArgumentConstraint last = argConstraints.get(argConstraints.size() - 1);
    argConstraints.set(argConstraints.size() - 1, new NegatingArgumentConstraint(last));
    return this;
  }

  public static final String ADD_CONSTANT_RESPONSE = "addConstantResponse";
  public InteractionBuilder addConstantResponse(Object constant) {
    responseGeneratorChain.addFirst(constant instanceof Wildcard ?
        new WildcardResponseGenerator() : new ConstantResponseGenerator(constant));
    return this;
  }

  public static final String ADD_CODE_RESPONSE = "addCodeResponse";
  public InteractionBuilder addCodeResponse(Closure closure) {
    responseGeneratorChain.addFirst(new CodeResponseGenerator(closure));
    return this;
  }

  public static final String ADD_ITERABLE_RESPONSE = "addIterableResponse";
  public InteractionBuilder addIterableResponse(Object iterable) {
    responseGeneratorChain.addFirst(new IterableResponseGenerator(iterable));
    return this;
  }

  public static final String BUILD = "build";
  public IMockInteraction build() {
    return new MockInteraction(line, column, text, minCount, maxCount, invConstraints,
        responseGeneratorChain.isEmpty() ? new DefaultResponseGenerator() : responseGeneratorChain);
  }

  private static int convertCount(Object count, boolean inclusive) {
    if (!(count instanceof Number))
      throw new InvalidSpecException("invocation count must be a number");

    int intCount = ((Number)count).intValue();
    if (!inclusive) intCount--;
    if (intCount < 0)
      throw new InvalidSpecException("invocation count must be >= 0");
    return intCount;
  }
}
