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

package org.spockframework.mock.constraint;

import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.runtime.GroovyRuntimeUtil;

import groovy.lang.Closure;

import java.util.Objects;

/**
 *
 * @author Peter Niederwieser
 */
public class CodeArgumentConstraint implements IArgumentConstraint {
  private final Closure<?> code;

  public CodeArgumentConstraint(Closure<?> code) {
    this.code = code;
  }

  @Override
  public boolean isDeclarationEqualTo(IArgumentConstraint other) {
    if (other instanceof CodeArgumentConstraint) {
      CodeArgumentConstraint o = (CodeArgumentConstraint) other;
      return Objects.equals(this.code, o.code);
    }
    return false;
  }

  @Override
  public boolean isSatisfiedBy(Object argument) {
    try {
      GroovyRuntimeUtil.invokeClosure(code, argument);
      return true;
    } catch (AssertionError ignore) {
      return false;
    }
  }

  @Override
  public String describeMismatch(Object argument) {
    try {
      GroovyRuntimeUtil.invokeClosure(code, argument);
    } catch (AssertionError ae) {
      return ae.getMessage();
    }
    return "<Code argument did not match>";
  }
}
