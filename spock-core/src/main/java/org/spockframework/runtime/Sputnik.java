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

package org.spockframework.runtime;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import org.junit.runners.model.RunnerScheduler;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.IncompatibleGroovyVersionException;
import org.spockframework.util.VersionChecker;

import java.util.List;

/**
 * A JUnit runner for Spock specifications. There is no need to put
 * <tt>@RunWith(Sputnik)</tt> on a specification because the <tt>RunWith</tt>
 * annotation is inherited from class <tt>spock.lang.Specification</tt>.
 * In case you wondered, Sputnik is a combination of the words "Spock" and "JUnit".
 *
 * @author Peter Niederwieser
 */
// TODO: check if StoppedByUserException thrown in Notifier.fireTestStarted() is handled correctly on our side
public class Sputnik extends ParentRunner implements Filterable, Sortable {
  private final Class<?> clazz;
  private SpecInfo spec;
  private boolean extensionsRun = false;
  private boolean descriptionGenerated = false;
  private RunnerScheduler scheduler = new SequentialRunnerScheduler();

  public Sputnik(Class<?> clazz) throws InitializationError {
    super(Object.class); // fake class - we need ParentRunner only to obtain RunnerScheduler
    try {
      VersionChecker.checkGroovyVersion("JUnit runner");
    } catch (IncompatibleGroovyVersionException e) {
      throw new InitializationError(e);
    }
    this.clazz = clazz;
  }

  @Override
  protected List getChildren() {
    throw new UnsupportedOperationException("workflow from ParentRunner not supported");
  }

  @Override
  protected Description describeChild(Object child) {
    throw new UnsupportedOperationException("workflow from ParentRunner not supported");
  }

  @Override
  protected void runChild(Object child, RunNotifier notifier) {
    throw new UnsupportedOperationException("workflow from ParentRunner not supported");
  }

  @Override
  public void setScheduler(RunnerScheduler scheduler) {
    super.setScheduler(scheduler);
    this.scheduler = scheduler;
  }

  public Description getDescription() {
    runExtensionsIfNecessary();
    generateSpecDescriptionIfNecessary();
    return getSpec().getDescription();
  }

  public void run(RunNotifier notifier) {
    runExtensionsIfNecessary();
    generateSpecDescriptionIfNecessary();
    RunContext.get().createSpecRunner(getSpec(), notifier, new Scheduler(scheduler, false)).run();
    scheduler.finished();
  }

  public void filter(Filter filter) throws NoTestsRemainException {
    invalidateSpecDescription();
    getSpec().filterFeatures(new JUnitFilterAdapter(filter));
    if (allFeaturesExcluded())
      throw new NoTestsRemainException();
  }

  public void sort(Sorter sorter) {
    invalidateSpecDescription();
    getSpec().sortFeatures(new JUnitSorterAdapter(sorter));
  }

  private SpecInfo getSpec() {
    if (spec == null) {
      spec = new SpecInfoBuilder(clazz).build();
      new JUnitDescriptionGenerator(spec).describeSpecMethods();
    }
    return spec;
  }

  private void runExtensionsIfNecessary() {
    if (extensionsRun) return;
    RunContext.get().createExtensionRunner(getSpec()).run();
    extensionsRun = true;
  }

  private void generateSpecDescriptionIfNecessary() {
    if (descriptionGenerated) return;
    new JUnitDescriptionGenerator(getSpec()).describeSpec();
    descriptionGenerated = true;
  }

  private void invalidateSpecDescription() {
    descriptionGenerated = false;
  }

  private boolean allFeaturesExcluded() {
    for (FeatureInfo feature: getSpec().getAllFeatures())
      if (!feature.isExcluded()) return false;

    return true;
  }
}
