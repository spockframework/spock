package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;
import spock.config.RunnerConfiguration;

import java.util.*;
import java.util.function.*;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import static java.util.Collections.emptySet;

class ClassSelectorResolver implements SelectorResolver {

  private final Predicate<String> classNameFilter;
  private final RunContext runContext;

  ClassSelectorResolver(Predicate<String> classNameFilter, RunContext runContext) {
    this.classNameFilter = classNameFilter;
    this.runContext = runContext;
  }

  @Override
  public Resolution resolve(ClassSelector selector, Context context) {
    return resolveClass(selector.getJavaClass(), context);
  }

  @Override
  public Resolution resolve(UniqueIdSelector selector, Context context) {
    UniqueId.Segment lastSegment = selector.getUniqueId().getLastSegment();
    if ("spec".equals(lastSegment.getType())) {
      String className = lastSegment.getValue();
      Class<?> specClass = ReflectionSupport.tryToLoadClass(className).getOrThrow(SpockException::new);
      return resolveClass(specClass, context);
    }
    return Resolution.unresolved();
  }

  private Resolution resolveClass(Class<?> specClass, Context context) {
    if (SpecUtil.isRunnableSpec(specClass) && classNameFilter.test(specClass.getName())) {
      return context
        .addToParent(parent -> {
          SpecInfo specInfo = new SpecInfoBuilder(specClass).build();
          specInfo.getAllFeatures().forEach(featureInfo -> featureInfo.setExcluded(true));
          UniqueId uniqueId = parent.getUniqueId().append("spec", specInfo.getReflection().getName());
          return Optional.of(new SpecNode(uniqueId,
            runContext.getConfiguration(RunnerConfiguration.class),
            specInfo));
        })
        .map(this::toResolution)
        .orElse(Resolution.unresolved());
    }
    return Resolution.unresolved();
  }

  private Resolution toResolution(SpecNode specNode) {
    return Resolution.match(Match.exact(specNode, features(specNode.getNodeInfo())));
  }

  private Supplier<Set<? extends DiscoverySelector>> features(SpecInfo specInfo) {
    return () -> {
      specInfo.getAllFeatures().forEach(featureInfo -> featureInfo.setExcluded(false));
      return emptySet();
    };
  }
}
