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

package org.spockframework.runtime;

/**
 * Indicates that a spec or feature should be skipped. May only be
 * thrown by an interceptor around SpecInfo or FeatureInfo.
 * 
 * Note: Throwing this exception will lead to wrong JUnit run counts.
 * If you can tell beforehand that a spec or feature should be skipped,
 * use the "skipped" flag on SpecInfo/FeatureInfo instead.
 * 
 * @author Peter Niederwieser
 */
public class SkipSpecOrFeatureException extends RuntimeException {
  private final String reason;

  public SkipSpecOrFeatureException(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }
}
