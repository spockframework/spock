/*
 * Copyright 2026 the original author or authors.
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

package org.spockframework.runtime;

import groovy.lang.Closure;
import org.spockframework.util.Beta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes a single data provider of a standalone {@code @DataProvider} method.
 * <p>
 * Instances are constructed by code the {@code @DataProvider} transform generates;
 * this class is not intended to be used directly.
 *
 * @since 2.5
 */
@Beta
public class StandaloneDataProviderDescriptor {
  private final Closure<?> code;
  private final List<String> dataVariables;
  private final List<String> previousDataTableVariables;
  private final int line;

  public StandaloneDataProviderDescriptor(Closure<?> code, String[] dataVariables,
                                          String[] previousDataTableVariables, int line) {
    this.code = code;
    this.dataVariables = Collections.unmodifiableList(Arrays.asList(dataVariables));
    this.previousDataTableVariables = Collections.unmodifiableList(Arrays.asList(previousDataTableVariables));
    this.line = line;
  }

  Closure<?> getCode() {
    return code;
  }

  List<String> getDataVariables() {
    return dataVariables;
  }

  List<String> getPreviousDataTableVariables() {
    return previousDataTableVariables;
  }

  int getLine() {
    return line;
  }
}
