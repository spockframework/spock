package org.spockframework.runtime;

import org.spockframework.util.SpockReleaseInfo;

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
//    rootTestDescriptor.getRunContext().getConfiguration()
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
}
