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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import spock.util.environment.Jvm;
import spock.util.environment.OperatingSystem;

/**
 * The context (delegate) for a {@link spock.lang.Requires} or {@link spock.lang.IgnoreIf} condition.
 */
public class PreconditionContext {
  private static final Pattern JAVA_VERSION = Pattern.compile("(\\d+\\.\\d+).*");

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
   * Returns the current JVM's system properties.
   *
   * @return the current JVM's system properties
   *
   * @deprecated use {@link #getSys()} instead
   */
  @Deprecated
  public Properties getProperties() {
    return getSys();
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
   * Returns the current JVM's Java specification version.
   * Examples for valid values are {@code 1.6} and {@code 1.7}.
   *
   * @return the current JVM's Java specification version
   */
  public BigDecimal getJavaVersion() {
    String version = System.getProperty("java.specification.version");
    return new BigDecimal(version);
  }
}
