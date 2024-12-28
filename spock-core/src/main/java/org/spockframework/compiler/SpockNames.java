package org.spockframework.compiler;

import org.spockframework.mock.ISpockMockObject;

public class SpockNames {
  public static final String ERROR_COLLECTOR = "$spock_errorCollector";
  public static final String FAILED_BLOCK = "$spock_failedBlock";
  public static final String OLD_VALUE = "$spock_oldValue";
  public static final String SPOCK_EX = "$spock_ex";
  public static final String SPOCK_FEATURE_THROWABLE = "$spock_feature_throwable";
  public static final String SPOCK_TMP_THROWABLE = "$spock_tmp_throwable";
  public static final String SPOCK_VALUE = "$spock_value";
  public static final String VALUE_RECORDER = "$spock_valueRecorder";
  /**
   * Name of the method {@link ISpockMockObject#$spock_get()}.
   */
  public static final String SPOCK_GET = "$spock_get";
  /**
   * Name of the method {@link ISpockMockObject#$spock_mockInteractionValidator()}.
   */
  public static final String SPOCK_MOCK_INTERATION_VALIDATOR = "$spock_mockInteractionValidator";
}
