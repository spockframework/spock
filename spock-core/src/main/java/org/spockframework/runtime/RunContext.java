/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import java.util.*;

import org.junit.runner.notification.RunNotifier;

import org.spockframework.builder.DelegatingScript;
import org.spockframework.runtime.condition.*;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.*;

import spock.config.RunnerConfiguration;

public class RunContext {
  private static final ThreadLocal<LinkedList<RunContext>> contextStacks =
      new ThreadLocal<LinkedList<RunContext>>() {
        protected LinkedList<RunContext> initialValue() {
          return new LinkedList<RunContext>();
        }
      };

  private final List<Class<?>> extensionClasses;
  private final ExtensionRegistry extensionRegistry;

  private final List<Object> configurations = new ArrayList<Object>();
  private final RunnerConfiguration runnerConfiguration = new RunnerConfiguration();

  private final IObjectRenderer<Object> diffedObjectRenderer = createDiffedObjectRenderer();

  private RunContext(@Nullable DelegatingScript configurationScript, List<Class<?>> extensionClasses) {
    this.extensionClasses = extensionClasses;

    configurations.add(runnerConfiguration);

    extensionRegistry = new ExtensionRegistry(extensionClasses, configurations);
    extensionRegistry.loadExtensions();

    if (configurationScript != null) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.build(configurations, configurationScript);
    }
  }

  public ExtensionRunner createExtensionRunner(SpecInfo spec) {
    return new ExtensionRunner(spec, extensionRegistry.getExtensions());
  }

  public ParameterizedSpecRunner createSpecRunner(SpecInfo spec, RunNotifier notifier) {
    return new ParameterizedSpecRunner(spec,
        new JUnitSupervisor(spec, notifier, createStackTraceFilter(spec), diffedObjectRenderer));
  }

  @Nullable
  public <T> T getConfiguration(Class<T> type) {
    for (Object config : configurations)
      if (config.getClass() == type) return type.cast(config);

    return null;
  }

  private IStackTraceFilter createStackTraceFilter(SpecInfo spec) {
    return runnerConfiguration.filterStackTrace ? new StackTraceFilter(spec) : new DummyStackTraceFilter();
  }

  private IObjectRenderer<Object> createDiffedObjectRenderer() {
    IObjectRendererService service = new ObjectRendererService();

    service.addRenderer(Object.class, new DiffedObjectAsBeanRenderer());

    DiffedObjectAsStringRenderer asStringRenderer = new DiffedObjectAsStringRenderer();
    service.addRenderer(CharSequence.class, asStringRenderer);
    service.addRenderer(Number.class, asStringRenderer);
    service.addRenderer(Character.class, asStringRenderer);
    service.addRenderer(Boolean.class, asStringRenderer);

    service.addRenderer(Collection.class, new DiffedCollectionRenderer());
    service.addRenderer(Set.class, new DiffedSetRenderer(true));
    service.addRenderer(SortedSet.class, new DiffedSetRenderer(false));
    service.addRenderer(Map.class, new DiffedMapRenderer(true));
    service.addRenderer(SortedMap.class, new DiffedMapRenderer(false));

    DiffedArrayRenderer arrayRenderer = new DiffedArrayRenderer();
    service.addRenderer(Object[].class, arrayRenderer);
    service.addRenderer(byte[].class, arrayRenderer);
    service.addRenderer(short[].class, arrayRenderer);
    service.addRenderer(int[].class, arrayRenderer);
    service.addRenderer(long[].class, arrayRenderer);
    service.addRenderer(float[].class, arrayRenderer);
    service.addRenderer(double[].class, arrayRenderer);
    service.addRenderer(char[].class, arrayRenderer);
    service.addRenderer(boolean[].class, arrayRenderer);

    return service;
  }

  public static <T, U extends Throwable> T withNewContext(@Nullable DelegatingScript configurationScript,
      List<Class<?>> extensionClasses, boolean inheritParentExtensions,
      IThrowableFunction<RunContext, T, U> command) throws U {
    List<Class<?>> allExtensionClasses = new ArrayList<Class<?>>(extensionClasses);
    if (inheritParentExtensions) allExtensionClasses.addAll(getCurrentExtensions());
    
    RunContext context = new RunContext(configurationScript, allExtensionClasses);
    LinkedList<RunContext> contextStack = contextStacks.get();
    contextStack.addFirst(context);
    try {
      return command.apply(context);
    } finally {
      contextStack.removeFirst();
    }
  }

  public static RunContext get() {
    LinkedList<RunContext> contextStack = contextStacks.get();
    RunContext context = contextStack.peek();
    if (context == null) {
      context = createBottomContext();
      contextStack.addFirst(context);
    }
    return context;
  }

  private static List<Class<?>> getCurrentExtensions() {
    RunContext context = contextStacks.get().peek();
    if (context == null) return Collections.emptyList();
    return context.extensionClasses;
  }
  
  // This context will stay around until the thread dies.
  // It would be more accurate to remove the context once the test run
  // has finished, but the JUnit Runner SPI doesn't provide an adequate hook.
  // That said, since most environments fork a new JVM for each test run,
  // this shouldn't be much of a problem in practice.
  private static RunContext createBottomContext() {
    DelegatingScript script = new ConfigurationScriptLoader().loadAutoDetectedScript();
    List<Class<?>> classes = new ExtensionClassesLoader().loadClassesFromDefaultLocation();
    return new RunContext(script, classes);
  }
}
