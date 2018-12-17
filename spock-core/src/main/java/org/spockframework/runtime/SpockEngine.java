package org.spockframework.runtime;

import java.util.Optional;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

import spock.lang.Specification;

public class SpockEngine extends HierarchicalTestEngine<SpockExecutionContext> {
  @Override
  protected SpockExecutionContext createExecutionContext(ExecutionRequest request) {
    return new SpockExecutionContext();
  }

  @Override
  public String getId() {
    return "spock";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    RunContext runContext = RunContext.createBottomContext(); // TODO cleanup
    SpockNodeGenerator spockNodeGenerator = new SpockNodeGenerator(uniqueId, runContext);
    SpockEngineDescriptor engineDescriptor = new SpockEngineDescriptor(uniqueId, runContext);
    discoveryRequest.getSelectorsByType(ClassSelector.class).forEach(classSelector -> {
        if (Specification.class.isAssignableFrom(classSelector.getJavaClass())) {
          engineDescriptor.addChild(spockNodeGenerator.describeSpec(classSelector.getJavaClass()));
        }
      }
    );
    return engineDescriptor;
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
    return Optional.empty(); // TODO later
  }
}
