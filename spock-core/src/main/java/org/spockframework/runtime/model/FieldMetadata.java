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

package org.spockframework.runtime.model;

import java.lang.annotation.*;

/**
 * Internal metadata about a field, from which the runtime model is built.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldMetadata {
  String NAME = "name";
  String ORDINAL = "ordinal";
  String LINE = "line";
  String INITIALIZER = "initializer";

  String name();
  int ordinal();
  int line();
  // This should not have a default, but to make it backwards compatible it has to have one.
  boolean initializer() default false;
}
