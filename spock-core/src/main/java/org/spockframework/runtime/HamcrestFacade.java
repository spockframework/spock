/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.util.*;

import java.lang.reflect.Method;

import org.hamcrest.*;

/**
 * Facade around Hamcrest API that works both with Hamcrest 1.1 and 1.2,
 * providing better failure descriptions if the latter is available.
 * HamcrestFacade.isMatcher() can safely be called no matter if Hamcrest
 * classes are available on the class path. The remaining methods assume
 * that Hamcrest classes are available.
 *
 * @author Peter Niederwieser
 */
public abstract class HamcrestFacade {
  private static final boolean hamcrestAvailable = ReflectionUtil.isClassAvailable("org.hamcrest.Matcher");

  public static boolean isMatcher(Object obj) {
    return hamcrestAvailable && HamcrestFacadeImpl.isMatcher(obj);
  }

  public static boolean matches(Object matcher, Object value) {
    return HamcrestFacadeImpl.matches(matcher, value);
  }

  public static String getFailureDescription(Object matcher, Object value, @Nullable String message) {
    return HamcrestFacadeImpl.getFailureDescription(matcher, value, message);
  }

  private abstract static class HamcrestFacadeImpl {
    static final Method describeMismatchMethod =
        ReflectionUtil.getMethodByName(Matcher.class, "describeMismatch");

    static boolean isMatcher(Object obj) {
      return obj instanceof Matcher;
    }

    static boolean matches(Object matcher, Object value) {
      return ((Matcher) matcher).matches(value);
    }

    static String getFailureDescription(Object matcher, Object value, @Nullable String message) {
      Description description = new StringDescription();

      if (message != null) {
        description.appendText(message);
        description.appendText("\n\n");
      }

      description.appendText("Expected: ")
          .appendDescriptionOf((Matcher) matcher);

      if (describeMismatchMethod == null) { // 1.1
        description.appendText("\n     got: ")
            .appendValue(value);
      } else { // 1.2
        description.appendText("\n     but: ");
        ReflectionUtil.invokeMethod(matcher, describeMismatchMethod, value, description);
      }

      return description.toString();
    }
  }
}

