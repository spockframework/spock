/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.builder.DelegatingScript;
import org.spockframework.runtime.condition.*;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.*;
import spock.config.RunnerConfiguration;

import java.io.File;
import java.security.AccessControlException;
import java.util.*;

import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import static java.util.Collections.emptyList;

public class RunContext implements EngineExecutionContext {
  private static final InheritableThreadLocal<LinkedList<RunContext>> contextStacks = new InheritableThreadLocal<LinkedList<RunContext>>() {

    @Override
    protected LinkedList<RunContext> initialValue() {
      return new LinkedList<>();
    }

    @Override
    protected LinkedList<RunContext> childValue(LinkedList<RunContext> parentValue) {
      if (parentValue == null) {
        return new LinkedList<>();
      }
      // fork the parent list
      return new LinkedList<>(parentValue);
    }
  };

  private final String name;
  private final File spockUserHome;
  private final DelegatingScript configurationScript;
  private final List<Class<? extends IGlobalExtension>> globalExtensionClasses;
  private final List<Class<?>> globalConfigClasses;
  private final GlobalExtensionRegistry globalExtensionRegistry;

  private final IObjectRenderer<Object> diffedObjectRenderer = createDiffedObjectRenderer();

  private RunContext(String name, File spockUserHome,
                     @Nullable DelegatingScript configurationScript,
                     List<Class<? extends IGlobalExtension>> globalExtensionClasses,
                     List<Class<?>> globalConfigClasses) {
    this.name = name;
    this.spockUserHome = spockUserHome;
    this.configurationScript = configurationScript;
    this.globalExtensionClasses = globalExtensionClasses;
    this.globalConfigClasses = globalConfigClasses;
    globalExtensionRegistry = new GlobalExtensionRegistry(globalExtensionClasses, globalConfigClasses);
  }

  private void start() {
    globalExtensionRegistry.initializeGlobalExtensions();

    if (configurationScript != null) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.build(globalExtensionRegistry, configurationScript);
    }

    globalExtensionRegistry.startGlobalExtensions();
  }

  void stop() {
    globalExtensionRegistry.stopGlobalExtensions();
  }

  public String getName() {
    return name;
  }

  // TODO: make it possible for configuration class to get at spock user home (w/o causing StackOverflowError)
  public File getSpockUserHome() {
    return spockUserHome;
  }

  public ExtensionRunner createExtensionRunner(SpecInfo spec) {
    return new ExtensionRunner(spec, globalExtensionRegistry, globalExtensionRegistry);
  }

  public ExtensionRunner createExtensionRunner() {
    return new ExtensionRunner(globalExtensionRegistry, globalExtensionRegistry);
  }

  public PlatformParameterizedSpecRunner createSpecRunner(SpecInfo spec) {
    return new PlatformParameterizedSpecRunner(
      new MasterRunSupervisor(spec, createStackTraceFilter(spec), diffedObjectRenderer));
  }

  @Nullable
  public <T> T getConfiguration(Class<T> type) {
    return globalExtensionRegistry.getConfigurationByType(type);
  }

  private IStackTraceFilter createStackTraceFilter(SpecInfo spec) {
    RunnerConfiguration runnerConfig = globalExtensionRegistry.getConfigurationByType(RunnerConfiguration.class);
    return runnerConfig.filterStackTrace ? new StackTraceFilter(spec) : new DummyStackTraceFilter();
  }

  private IObjectRenderer<Object> createDiffedObjectRenderer() {
    IObjectRendererService service = new ObjectRendererService();

    service.addRenderer(Object.class, new DiffedObjectAsBeanRenderer());

    DiffedObjectAsStringRenderer asStringRenderer = new DiffedObjectAsStringRenderer();
    service.addRenderer(CharSequence.class, asStringRenderer);
    service.addRenderer(Number.class, asStringRenderer);
    service.addRenderer(Character.class, asStringRenderer);
    service.addRenderer(Boolean.class, asStringRenderer);

    service.addRenderer(Class.class, new DiffedClassRenderer());
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

  public static <T, U extends Throwable> T withNewContext(String name, File spockUserHome,
                                                          @Nullable DelegatingScript configurationScript,
                                                          List<Class<? extends IGlobalExtension>> extensionClasses,
                                                          List<Class<?>> configClasses,
                                                          boolean inheritParentExtensions,
                                                          IThrowableFunction<RunContext, T, U> command) throws U {
    List<Class<? extends IGlobalExtension>> allExtensionClasses = new ArrayList<>(extensionClasses);
    List<Class<?>> allConfigClasses = new ArrayList<>(configClasses);
    if (inheritParentExtensions) {
      allExtensionClasses.addAll(getCurrentExtensions());
      allConfigClasses.addAll(getCurrentConfigs());
    }

    RunContext context = new RunContext(name, spockUserHome, configurationScript, allExtensionClasses, allConfigClasses);
    Deque<RunContext> contextStack = contextStacks.get();
    contextStack.addFirst(context);
    try {
      context.start();
      return command.apply(context);
    } finally {
      contextStack.removeFirst();
      context.stop();
    }
  }

  public static RunContext get() {
    LinkedList<RunContext> contextStack = contextStacks.get();
    RunContext context = contextStack.peek();
    if (context == null) {
      context = createBottomContext();
      contextStack.addFirst(context);
      final RunContext bottomContext = context;
      try {
        Runtime.getRuntime().addShutdownHook(
          new Thread(bottomContext::stop, "org.spockframework.runtime.RunContext.stop()"));
      } catch (AccessControlException ignored) {
        // GAE doesn't support creating a new thread
      }
      context.start();
    }
    return context;
  }

  private static List<Class<? extends IGlobalExtension>> getCurrentExtensions() {
    RunContext context = contextStacks.get().peek();
    if (context == null) return emptyList();
    return context.globalExtensionClasses;
  }

  private static List<Class<?>> getCurrentConfigs() {
    RunContext context = contextStacks.get().peek();
    if (context == null) return emptyList();
    return context.globalConfigClasses;
  }

  // This context will stay around until the thread dies.
  // It would be more accurate to remove the context once the test run
  // has finished, but the JUnit Runner SPI doesn't provide an adequate hook.
  // That said, since most environments fork a new JVM for each test run,
  // this shouldn't be much of a problem in practice.
  static RunContext createBottomContext() {
    File spockUserHome = SpockUserHomeUtil.getSpockUserHome();
    DelegatingScript script = new ConfigurationScriptLoader(spockUserHome).loadAutoDetectedScript();
    ExtensionClassesLoader extensionClassesLoader = new ExtensionClassesLoader();
    List<Class<? extends IGlobalExtension>> classes = extensionClassesLoader.loadExtensionClassesFromDefaultLocation();
    List<Class<?>> configs = extensionClassesLoader.loadConfigClassesFromDefaultLocation();
    return new RunContext("default", spockUserHome, script, classes, configs);
  }
}
