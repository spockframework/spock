/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import static java.util.Collections.emptyMap;

import java.math.BigDecimal;
import java.util.*;

import spock.lang.IgnoreIf;
import spock.lang.PendingFeatureIf;
import spock.lang.Requires;
import spock.util.environment.Jvm;
import spock.util.environment.OperatingSystem;

/**
 * The context (delegate) for a {@link Requires}, {@link IgnoreIf} or {@link PendingFeatureIf} condition.
 */
public class PreconditionContext {
  private final Object theSharedInstance;
  private final Object theInstance;
  private final Map<String, Object> dataVariables;

  public PreconditionContext() {
    this(null, null, emptyMap());
  }

  public PreconditionContext(Object sharedInstance, Object instance, Map<String, Object> dataVariables) {
    this.theSharedInstance = sharedInstance;
    this.theInstance = instance;
    this.dataVariables = new StrictHashMap<>(dataVariables);
  }

  /**
   * Returns the current JVM's environment variables.
   *
   * @return the current JVM's environment variables
   */
  public Map<String, String> getEnv() {
    return System.getenv();
  }

  /**
   * Returns the current JVM's system properties.
   *
   * @return the current JVM's system properties
   */
  public Properties getSys() {
    return System.getProperties();
  }

  /**
   * Returns the current operating system.
   *
   * @return the current operating system
   */
  public OperatingSystem getOs() {
    return OperatingSystem.getCurrent();
  }

  /**
   * Returns the current JVM.
   *
   * @return the current JVM
   */
  public Jvm getJvm() {
    return Jvm.getCurrent();
  }

  /**
   * Returns the Test Instance
   * <p>
   * If accessed, the instance' setup will run before this can be evaluated.
   * @since 2.0
   * @return the test instance
   */
  public Object getInstance() {
    if (theInstance == null) {
      throw new InstanceContextException();
    }
    return theInstance;
  }

  /**
   * Returns the Shared Test Instance
   * <p>
   * If accessed, the Specification will run and initialize the shared instance before this can be evaluated.
   *
   * @since 2.1
   * @return the shared instance
   */
  public Object getShared() {
    if (theSharedInstance == null) {
      throw new SharedContextException();
    }
    return theSharedInstance;
  }

  /**
   * Returns the data variables for data-driven features.
   * <p>
   * This cannot be used in a Specification level condition.
   * <p>
   * If accessed, the instance' setup will run before this can be evaluated.
   *
   * @since 2.1
   * @return the data variables
   */
  public Map<String, Object> getData() {
    return dataVariables;
  }

  /**
   * Returns the current JVM's Java specification version.
   * Examples for valid values are {@code 1.6} and {@code 1.7}.
   *
   * @return the current JVM's Java specification version
   */
  public BigDecimal getJavaVersion() {
    String version = System.getProperty("java.specification.version");
    return new BigDecimal(version);
  }

  private static class StrictHashMap<K, V> extends HashMap<K, V> {
    public StrictHashMap(Map<K,V> map) {
      super(map);
    }

    @Override
    public V get(Object key) {
      if (!containsKey(key)) {
        throw new DataVariableContextException(key.toString());
      }
      return super.get(key);
    }

    @Override
    public V put(K key, V value) {
      throw new UnsupportedOperationException("Unmodifiable");
    }
  }

  public static class PreconditionContextException extends RuntimeException {}
  public static class SharedContextException extends PreconditionContextException {}
  public static class InstanceContextException extends PreconditionContextException {}
  public static class DataVariableContextException extends PreconditionContextException {
    private final String dataVariable;

    public DataVariableContextException(String dataVariable) {this.dataVariable = dataVariable;}

    public String getDataVariable() {
      return dataVariable;
    }
  }
}
