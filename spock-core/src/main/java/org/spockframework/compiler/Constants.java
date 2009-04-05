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

package org.spockframework.compiler;

import java.util.Arrays;
import java.util.List;

/**
 * Constants used throughout the SpeckL implementation.
 * 
 * @author Peter Niederwieser
 */
public abstract class Constants {
  // Tokens -------------------------------------------------------------------
  
  public static final String SETUP = "setup";

  public static final String GIVEN = "given";

  public static final String EXPECT = "expect";

  /**
   * Label name identifying a when-block.
   */
  public static final String WHEN = "when";

  /**
   * Label name identifying a then-block.
   */
  public static final String THEN = "then";

  /**
   * Label name identifying a cleanup-block.
   */
  public static final String CLEANUP = "cleanup";
  /**
   * Label name identifying a where-block.
   */
  public static final String WHERE = "where";

  public static final String AND = "and";

  public static final List<String> BLOCK_LABELS = Arrays.asList(SETUP, GIVEN, EXPECT, WHEN, THEN, CLEANUP, WHERE, AND);

  /**
   * Method name identifying a fixture method that is executed before each speck.
   */
  public static final String SETUP_SPECK = "setupSpeck";

  /**
   * Method name identifying a fixture method that is executed after each speck.
   */
  public static final String CLEANUP_SPECK = "cleanupSpeck";

  public static final List<String> FIXTURE_METHODS = Arrays.asList(SETUP, CLEANUP, SETUP_SPECK, CLEANUP_SPECK);

  public static final String MOCK = "Mock";

  public static final String THROWN = "thrown";

  public static final String INTERACTION = "interaction";

  public static final String OLD = "old";
}
