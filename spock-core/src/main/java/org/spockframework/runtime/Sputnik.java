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

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.VersionChecker;

/**
 * A JUnit runner for Spock specifications. There is no need to put
 * <tt>@RunWith(Sputnik)</tt> on a specification because the <tt>RunWith</tt>
 * annotation is inherited from class <tt>spock.lang.Specification</tt>.
 * In case you wondered, Sputnik is a combination of the words "Spock" and "JUnit".
 *
 * @author Peter Niederwieser
 */
// - check if StoppedByUserException thrown in Notifier.fireTestStarted() is handled correctly on our side
public class Sputnik extends Runner implements Filterable, Sortable {
  private final RunContext runContext;
  private final SpecInfoBuilder builder;
  private final SpecInfo spec;
  private boolean extensionsRun = false;
  private boolean descriptionAggregated = false;

  public Sputnik(Class<?> clazz) {
    VersionChecker.checkSpockAndGroovyVersionsAreCompatible("spec runner");

    runContext = RunContext.get();
    builder = runContext.createSpecInfoBuilder(clazz);
    spec = builder.build();
    new JUnitDescriptionGenerator(spec).attach();
  }

  public Description getDescription() {
    runExtensionsIfNecessary();
    aggregateDescriptionIfNecessary();
    return (Description) spec.getMetadata();
  }

  public void run(RunNotifier notifier) {
    runExtensionsIfNecessary();
    aggregateDescriptionIfNecessary();
    runContext.createSpecRunner(spec, notifier).run();
  }

  public void filter(Filter filter) throws NoTestsRemainException {
    spec.filterFeatures(new JUnitFilterAdapter(filter));
    if (allFeaturesExcluded())
      throw new NoTestsRemainException();
  }

  public void sort(Sorter sorter) {
    spec.sortFeatures(new JUnitSorterAdapter(sorter));
  }

  private void runExtensionsIfNecessary() {
    if (extensionsRun) return;
    builder.runExtensions();
    extensionsRun = true;
  }

  private void aggregateDescriptionIfNecessary() {
    if (descriptionAggregated) return;
    new JUnitDescriptionGenerator(spec).aggregate();
    descriptionAggregated = true;
  }

  private boolean allFeaturesExcluded() {
    for (FeatureInfo feature: spec.getAllFeatures())
      if (!feature.isExcluded()) return false;

    return true;
  }
}
