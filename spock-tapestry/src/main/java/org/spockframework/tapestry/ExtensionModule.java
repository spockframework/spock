/*
 * Copyright 2009, 2011 the original author or authors.
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

package org.spockframework.tapestry;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Marker;

/**
 * A Tapestry module that is started for every specification which uses Spock's
 * Tapestry extension.
 *
 * @author Peter Niederwieser
 */
@Marker(SpockTapestry.class)
public class ExtensionModule {
  public static ObjectLocator build(ObjectLocator locator) {
    return locator;
  }
}
