package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;

import java.util.*;
import java.util.function.*;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.discovery.SelectorResolver;

class ClassSelectorResolver implements SelectorResolver {

  private final Predicate<String> classNameFilter;

  ClassSelectorResolver(Predicate<String> classNameFilter) {
    this.classNameFilter = classNameFilter;
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
      SpecInfo specInfo = new SpecInfoBuilder(specClass).build();
      return context
        .addToParent(parent -> {
          specInfo.getAllFeatures().forEach(featureInfo -> featureInfo.setExcluded(true));
          UniqueId uniqueId = parent.getUniqueId().append("spec", specInfo.getReflection().getName());
          return Optional.of(new SpecNode(uniqueId, specInfo));
        })
        .map(specNode -> toResolution(specInfo, specNode))
        .orElse(Resolution.unresolved());
    }
    return Resolution.unresolved();
  }

  private Resolution toResolution(SpecInfo specInfo, SpecNode specNode) {
    return Resolution.match(Match.exact(specNode, features(specInfo)));
  }

  private Supplier<Set<? extends DiscoverySelector>> features(SpecInfo specInfo) {
    return () -> {
      specInfo.getAllFeatures().forEach(featureInfo -> featureInfo.setExcluded(false));
      return Collections.emptySet();
    };
  }
}
