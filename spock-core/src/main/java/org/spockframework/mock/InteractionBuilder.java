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

package org.spockframework.mock;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Closure;

import org.spockframework.lang.SpreadWildcard;
import org.spockframework.lang.Wildcard;
import org.spockframework.runtime.InvalidSpecException;

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
  private List<IInvocationConstraint> invConstraints = new ArrayList<IInvocationConstraint>();
  private List<Object> argNames;
  private List<IArgumentConstraint> argConstraints;
  private ResultGeneratorChain resultGenerators = new ResultGeneratorChain();

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
    if (!(target instanceof Wildcard))
      invConstraints.add(new IdenticalTargetConstraint(target));
    return this;
  }

  public static final String ADD_EQUAL_PROPERTY_NAME = "addEqualPropertyName";
  public InteractionBuilder addEqualPropertyName(String name) {
    if (name.equals(Wildcard.INSTANCE.toString()))
      invConstraints.add(WildcardMethodNameConstraint.INSTANCE);
    else
      invConstraints.add(new EqualPropertyNameConstraint(name));
    return this;
  }

  public static final String ADD_REGEX_PROPERTY_NAME = "addRegexPropertyName";
  public InteractionBuilder addRegexPropertyName(String regex) {
    invConstraints.add(new RegexPropertyNameConstraint(regex));
    return this;
  }

  public static final String ADD_EQUAL_METHOD_NAME = "addEqualMethodName";
  public InteractionBuilder addEqualMethodName(String name) {
    if (name.equals(Wildcard.INSTANCE.toString()))
      invConstraints.add(WildcardMethodNameConstraint.INSTANCE);
    else
      invConstraints.add(new EqualMethodNameConstraint(name));
    return this;
  }

  public static final String ADD_REGEX_METHOD_NAME = "addRegexMethodName";
  public InteractionBuilder addRegexMethodName(String regex) {
    invConstraints.add(new RegexMethodNameConstraint(regex));
    return this;
  }

  public static final String SET_ARG_LIST_KIND = "setArgListKind";
  public InteractionBuilder setArgListKind(boolean isPositional) {
    argConstraints = new ArrayList<IArgumentConstraint>();
    if (isPositional)
      invConstraints.add(new PositionalArgumentListConstraint(argConstraints));
    else {
      argNames = new ArrayList<Object>();
      invConstraints.add(new NamedArgumentListConstraint(argNames, argConstraints));
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

  public static final String ADD_CONSTANT_RESULT = "setConstantResult";
  public InteractionBuilder setConstantResult(Object constant) {
    resultGenerators.addFirst(constant instanceof Wildcard ? new DefaultResultGenerator() : new ConstantResultGenerator(constant));
    return this;
  }

  public static final String ADD_CODE_RESULT = "setCodeResult";
  public InteractionBuilder setCodeResult(Closure closure) {
    resultGenerators.addFirst(new CodeResultGenerator(closure));
    return this;
  }

  public static final String ADD_ITERABLE_RESULT = "setIterableResult";
  public InteractionBuilder setIterableResult(Object iterable) {
    resultGenerators.addFirst(new IterableResultGenerator(iterable));
    return this;
  }

  public static final String BUILD = "build";
  public IMockInteraction build() {
    if (resultGenerators.isEmpty())
      resultGenerators.addFirst(new DefaultResultGenerator());

    return new MockInteraction(line, column, text,
        minCount, maxCount, invConstraints, resultGenerators);
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
