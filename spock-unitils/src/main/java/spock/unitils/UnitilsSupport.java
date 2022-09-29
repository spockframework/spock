/*
 * Copyright 2009 the original author or authors.
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

package spock.unitils;

import java.lang.annotation.*;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.unitils.UnitilsExtension;

/**
 * Activates <a href="http://www.unitils.org">Unitils</a> support for a specification.
 * All Unitils features are supported.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(UnitilsExtension.class)
public @interface UnitilsSupport {}
