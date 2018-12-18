package org.spockframework.runtime;

import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.Optional;

import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

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
    RunContext runContext = RunContext.get(); // TODO cleanup

    SpockNodeGenerator spockNodeGenerator = new SpockNodeGenerator(uniqueId, runContext);
    SpockEngineDescriptor engineDescriptor = new SpockEngineDescriptor(uniqueId, runContext);
    discoveryRequest.getSelectorsByType(ClassSelector.class).forEach(classSelector -> {
      Class<?> javaClass = classSelector.getJavaClass();
      if(exclude(javaClass)) return;

      if (Specification.class.isAssignableFrom(javaClass)) {
          engineDescriptor.addChild(spockNodeGenerator.describeSpec(javaClass));
        }
      }
    );
    return engineDescriptor;
  }

  boolean exclude(Class<?> javaClass) {
    return Modifier.isAbstract(javaClass.getModifiers())
      || javaClass.isLocalClass()
      || javaClass.isAnonymousClass()
      || isNonStaticInnerclass(javaClass);
  }

  private boolean isNonStaticInnerclass(Class<?> javaClass) {
    return javaClass.isMemberClass() && !Modifier.isStatic(javaClass.getModifiers());
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
