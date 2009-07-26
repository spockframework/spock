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

package org.spockframework.runtime

import org.junit.runner.Runner
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier

/**
 * A JUnit runner for observing the behavior of JUnit's default runner.
 *
 * @author Peter Niederwieser
 */
class ObservableRunner extends Runner {
  Runner delegate

  ObservableRunner(Class clazz) {
    println "INIT"
    delegate = new BlockJUnit4ClassRunner(clazz)
  }

  public Description getDescription() {
    println "GET DESCRIPTION"
    return delegate.getDescription()
  }

  public void run(RunNotifier notifier) {
    println "RUN"
    delegate.run(new ObservableNotifier(delegate: notifier))
  }
}

class ObservableNotifier extends RunNotifier {
  RunNotifier delegate

  public void fireTestStarted(Description description) {
    println "STARTED"
    delegate.fireTestStarted(description)
  }

  public void fireTestFinished(Description description) {
    println "FINISHED"
    delegate.fireTestFinished(description);
  }

  public void fireTestIgnored(Description description) {
    println "IGNORED"
    delegate.fireTestIgnored(description);
  }
}