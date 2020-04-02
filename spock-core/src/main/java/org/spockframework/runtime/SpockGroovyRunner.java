/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.runtime;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.plugin.GroovyRunner;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import spock.lang.Specification;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class SpockGroovyRunner implements GroovyRunner {
  @Override
  public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
    return Specification.class.isAssignableFrom(scriptClass);
  }

  @Override
  public Object run(Class<?> scriptClass, GroovyClassLoader loader) {
    LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
      .request()
      .selectors(selectClass(scriptClass))
      .build();
    Launcher launcher = LauncherFactory.create();

    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);
    launcher.registerTestExecutionListeners(LoggingListener.forJavaUtilLogging());
    launcher.execute(request);

    TestExecutionSummary summary = listener.getSummary();
    System.out.printf("Spock launcher: passed=%d, failed=%d, skipped=%d, time=%dms%n",
      summary.getTestsSucceededCount(),
      summary.getTestsFailedCount(),
      summary.getTestsSkippedCount(),
      summary.getTimeFinished() - summary.getTimeStarted());
    if (!summary.getFailures().isEmpty()) {
      summary.printFailuresTo(new PrintWriter(System.out, true));
      throw new GroovyRuntimeException(summary.getFailures().get(0).getException());
    }

    return null;
  }
}
