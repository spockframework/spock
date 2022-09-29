/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.util;

import java.util.concurrent.TimeUnit;

public abstract class TimeUtil {
  public static double toSeconds(long value, TimeUnit unit) {
    if (unit.ordinal() >= TimeUnit.SECONDS.ordinal()) {
      return TimeUnit.SECONDS.convert(value, unit);
    }
    return value / (double) unit.convert(1, TimeUnit.SECONDS);
  }
}
