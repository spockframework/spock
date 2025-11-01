/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import static java.util.stream.Collectors.joining;

public class ClassUtil {
  public static @NotNull String nullSafeToString(@Nullable Class<?> clazz) {
    if (clazz != null)
      return clazz.toString();
    else
      return "null";
  }

  // not an overload because I couldn't fix
  // ambiguous overload errors in the spec
  public static @NotNull String allNullSafeToString(@Nullable Class<?>... clazzes) {
    if (clazzes != null)
      return Arrays.stream(clazzes)
             .map(ClassUtil::nullSafeToString)
             .collect(joining(", "));
    else
      return "";
  }
}
