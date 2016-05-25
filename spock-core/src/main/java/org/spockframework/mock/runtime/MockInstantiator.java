/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import java.util.List;

import org.objenesis.ObjenesisStd;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.Nullable;
import org.spockframework.util.ReflectionUtil;

public class MockInstantiator {
  private static final boolean objenesisAvailable = ReflectionUtil.isClassAvailable("org.objenesis.Objenesis");

  public static Object instantiate(Class<?> declaredType, Class<?> actualType, @Nullable List<Object> constructorArgs, boolean useObjenesis) {
    try {
      if (!declaredType.isInterface() && constructorArgs == null && useObjenesis && objenesisAvailable) {
        return ObjenesisInstantiator.instantiate(actualType);
      }
      return GroovyRuntimeUtil.invokeConstructor(actualType, constructorArgs == null ? null : constructorArgs.toArray());
    } catch (Exception e) {
      String msg = constructorArgs == null && useObjenesis && !objenesisAvailable ?
          ". To solve this problem, put Objenesis 1.2 or higher on the class path (recommended), or supply " +
              "constructor arguments (e.g. 'constructorArgs: [42]') that allow to construct an object of the mocked type." : null;
      throw new CannotCreateMockException(declaredType, msg, e);
    }
  }

  // inner class to defer class loading
  private static class ObjenesisInstantiator {
    static Object instantiate(Class<?> clazz) {
      return clazz.cast(new ObjenesisStd(false).newInstance(clazz));
    }
  }
}
