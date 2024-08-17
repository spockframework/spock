/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

import spock.config.ConfigurationObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration settings for the Timeout extension.
 *
 * <p>Example:
 * <pre>
 * timeout {
 *   // boolean, default false
 *   printThreadDumpsOnInterruptAttempts true
 *   // integer, default 3
 *   maxInterruptAttemptsWithThreadDumps 5
 *   // org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType, default JCMD
 *   threadDumpUtilityType threadDumpUtilityType.JSTACK
 *   // list of java.lang.Runnable, default []
 *   interruptAttemptListeners.add({ println('Timeout occurred!') })
 * }
 * </pre>
 *
 * @since 2.4
 */
@Beta
@ConfigurationObject("timeout")
public class TimeoutConfiguration {

  /**
   * If set to a valid duration, it will apply for all features that do not have a specific timeout set, default null.
   * @since 2.4
   */
  public @Nullable Duration globalTimeout = null;

  /**
   * Determines whether the global timeout will be applied to fixtures, default false.
   * @since 2.4
   */
  public boolean applyGlobalTimeoutToFixtures = false;

  /**
   * Determines whether thread dumps will be captured and logged on feature timeout or unsuccessful interrupt attempts, default false.
   */
  public boolean printThreadDumpsOnInterruptAttempts = false;

  /**
   * Maximum number of unsuccessful interrupts to log the thread dumps for, default 3.
   */
  public int maxInterruptAttemptsWithThreadDumps = 3;

  /**
   * Utility used to capture thread dumps, default {@link ThreadDumpUtilityType#JCMD}.
   */
  public ThreadDumpUtilityType threadDumpUtilityType = ThreadDumpUtilityType.JCMD;

  /**
   * Listeners to be invoked on method timeout or unsuccessful interrupt attempts, default empty.
   */
  public List<Runnable> interruptAttemptListeners = new ArrayList<>();
}
