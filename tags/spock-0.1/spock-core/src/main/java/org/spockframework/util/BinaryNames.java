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

package org.spockframework.util;

/**
 * Provides binary names of data processor and data provider methods
 * based on the binary name of a feature method.
 *
 * @author Peter Niederwieser
 */
public class BinaryNames {
  public static String getDataProcessorName(String featureName) {
    return featureName + "proc";
  }

  public static String getDataProviderName(String featureName, int providerIndex) {
    return featureName + "prov" + providerIndex;
  }
}
