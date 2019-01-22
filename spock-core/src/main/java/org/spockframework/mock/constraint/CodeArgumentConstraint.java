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

package org.spockframework.mock.constraint;

import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.runtime.GroovyRuntimeUtil;

import groovy.lang.Closure;
import org.junit.runners.model.MultipleFailureException;

/**
 *
 * @author Peter Niederwieser
 */
public class CodeArgumentConstraint implements IArgumentConstraint {
  private final Closure code;

  public CodeArgumentConstraint(Closure code) {
    this.code = code;
  }

  @Override
  public boolean isSatisfiedBy(Object argument) {
    try {
      GroovyRuntimeUtil.invokeClosure(code, argument);
      return true;
    } catch (AssertionError ignore) {
      return false;
    } catch (Exception e) {
      // MFE can't be catched directly as it thrown by sneakyThrows and not declared as a checked exception
      if (e instanceof MultipleFailureException) {
        return false;
      }
      throw e;
    }
  }

  @Override
  public String describeMismatch(Object argument) {
    try {
      GroovyRuntimeUtil.invokeClosure(code, argument);
    } catch (AssertionError ae) {
      return ae.getMessage();
    }  catch (Exception e) {
      if (e instanceof MultipleFailureException) {
        return e.getMessage();
      }
      throw e;
    }
    return "<Code argument did not match>";
  }
}
