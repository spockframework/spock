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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Identifiers used throughout the core.
 *
 * @author Peter Niederwieser
 */
public abstract class Identifiers {
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

  public static final String SETUP_METHOD = "setup";

  public static final String CLEANUP_METHOD = "cleanup";

  /**
   * Method name identifying a fixture method that is executed before each spec.
   */
  public static final String SETUP_SPEC_METHOD = "setupSpec";

  /**
   * Method name identifying a fixture method that is executed after each spec.
   */
  public static final String CLEANUP_SPEC_METHOD = "cleanupSpec";

  public static final List<String> FIXTURE_METHODS = Arrays.asList(SETUP_METHOD, CLEANUP_METHOD,
		  SETUP_SPEC_METHOD, CLEANUP_SPEC_METHOD);

  public static final String GET_SPECIFICATION_CONTEXT = "getSpecificationContext";

  public static final String WITH = "with";

  public static final String VERIFY_ALL = "verifyAll";

  public static final String STUB = "Stub";
  public static final String MOCK = "Mock";
  public static final String SPY = "Spy";
  public static final String GROOVY_STUB = "GroovyStub";
  public static final String GROOVY_MOCK = "GroovyMock";
  public static final String GROOVY_SPY = "GroovySpy";

  public static final String THROWN = "thrown";
  public static final String NOT_THROWN = "notThrown";
  public static final String NO_EXCEPTION_THROWN = "noExceptionThrown";

  public static final String INTERACTION = "interaction";

  public static final String OLD = "old";

  public static final Set<String> BUILT_IN_METHODS = new HashSet<String>(Arrays.asList(THROWN, NOT_THROWN,
      NO_EXCEPTION_THROWN, OLD, WITH, VERIFY_ALL, INTERACTION, STUB, MOCK, SPY, GROOVY_STUB, GROOVY_MOCK, GROOVY_SPY));

  public static final Set<String> TEST_DOUBLE_METHODS = new HashSet<String>(
      Arrays.asList(STUB, MOCK, SPY, GROOVY_STUB, GROOVY_MOCK, GROOVY_SPY));

  public static final Set<String> EXCEPTION_CONDITION_METHODS = new HashSet<String>(Arrays.asList(THROWN, NOT_THROWN, NO_EXCEPTION_THROWN));
}
