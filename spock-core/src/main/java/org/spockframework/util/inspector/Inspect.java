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

package org.spockframework.util.inspector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a declaration in Groovy code so that it can be easily accessed using
 * AstInspector.
 *
 * @author Peter Niederwieser (pniederw@gmail.com)
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Inspect {
  /**
   * The name of the inspection mark.
   */
  String value();
}
