package org.spockframework.runtime;

import org.spockframework.util.SpockReleaseInfo;
import spock.config.RunnerConfiguration;

import java.util.Optional;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.hierarchical.*;

public class SpockEngine extends HierarchicalTestEngine<SpockExecutionContext> {

  @Override
  public String getId() {
    return "spock";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    RunContext runContext = RunContext.get();
    SpockEngineDescriptor engineDescriptor = new SpockEngineDescriptor(uniqueId, runContext);
    EngineDiscoveryRequestResolver.builder()
      .addClassContainerSelectorResolver(SpecUtil::isRunnableSpec)
      .addSelectorResolver(context -> new ClassSelectorResolver(context.getClassNameFilter()))
      .addSelectorResolver(new MethodSelectorResolver())
      .build()
      .resolve(discoveryRequest, engineDescriptor);

    return new SpockEngineDiscoveryPostProcessor()
      .postProcessEngineDescriptor(uniqueId, runContext, engineDescriptor);
  }

  @Override
  protected SpockExecutionContext createExecutionContext(ExecutionRequest request) {
    return new SpockExecutionContext(request.getEngineExecutionListener());
  }

  @Override
  protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
    SpockEngineDescriptor rootTestDescriptor = (SpockEngineDescriptor)request.getRootTestDescriptor();
    RunnerConfiguration configuration = rootTestDescriptor.getRunContext()
      .getConfiguration(RunnerConfiguration.class);
    if (configuration.parallel.enabled) {
      return new ForkJoinPoolHierarchicalTestExecutorService(new ConfigurationParametersAdapter());
    }
    return super.createExecutorService(request);
  }

  @Override
  public Optional<String> getGroupId() {
    return Optional.of("org.spockframework");
  }

  @Override
  public Optional<String> getArtifactId() {
    return Optional.of("spock-core");
  }

  @Override
  public Optional<String> getVersion() {
    return Optional.of(SpockReleaseInfo.getVersion().toString());
  }

  // TODO Replace
  private static class ConfigurationParametersAdapter implements ConfigurationParameters {
    @Override
    public Optional<String> get(String key) {
      switch (key) {
        case "strategy":
          return Optional.of("dynamic");
        case "dynamic.factor":
          return Optional.of("1.0");
        default:
          return Optional.empty();
      }
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
      return Optional.empty();
    }

    @Override
    public int size() {
      return 2;
    }
  }
}
