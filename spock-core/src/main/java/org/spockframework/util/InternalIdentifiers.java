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

package org.spockframework.util;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * Internal identifiers in generated byte code.
 *
 * @author Peter Niederwieser
 */
public class InternalIdentifiers {
  public static final String INITIALIZER_METHOD = "$spock_initializeFields";
  public static final String SHARED_INITIALIZER_METHOD = "$spock_initializeSharedFields";

  public static final List<String> INITIALIZER_METHODS = asList(INITIALIZER_METHOD, SHARED_INITIALIZER_METHOD);

  public static final List<String> INITIALIZER_AND_FIXTURE_METHODS;

  static {
    INITIALIZER_AND_FIXTURE_METHODS = new ArrayList<>();
    INITIALIZER_AND_FIXTURE_METHODS.addAll(INITIALIZER_METHODS);
    INITIALIZER_AND_FIXTURE_METHODS.addAll(Identifiers.FIXTURE_METHODS);
  }

  public static String getDataProcessorName(String featureName) {
    return featureName + "proc";
  }

  public static String getDataVariableMultiplicationsName(String featureName) {
    return featureName + "prods";
  }

  public static String getFilterName(String featureName) {
    return featureName + "filter";
  }

  public static String getDataProviderName(String featureName, int providerIndex) {
    return featureName + "prov" + providerIndex;
  }

  public static boolean isFeatureMethodName(String name) {
    return name.startsWith("$spock_feature");
  }

  public static String getSharedFieldName(String baseName) {
    return "$spock_sharedField_" + baseName;
  }
  public static String getFinalFieldName(String baseName) {
    return "$spock_finalField_" + baseName;
  }

  public static boolean isInternalName(String name) {
    return name.startsWith("$spock_");
  }
}
