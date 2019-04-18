package org.spockframework.runtime;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.spockframework.runtime.model.SpecInfo;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

class ClassSelectorResolver implements SelectorResolver {

  private final Predicate<String> classNameFilter;
  private final RunContext runContext;

  ClassSelectorResolver(Predicate<String> classNameFilter, RunContext runContext) {
    this.classNameFilter = classNameFilter;
    this.runContext = runContext;
  }

  @Override
  public Resolution resolve(ClassSelector selector, Context context) {
    return resolveClass(((ClassSelector) selector).getJavaClass(), context);
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
          UniqueId uniqueId = parent.getUniqueId().append("spec", specInfo.getReflection().getName());
          try {
            runContext.createExtensionRunner(specInfo).run();
          } catch (Exception e) {
            // TODO revisit, this should be handled on the platform level
            return Optional.of(new ErrorSpecNode(uniqueId, specInfo, e));
          }
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
    return () -> specInfo.getAllFeaturesInExecutionOrder().stream()
      .map(feature -> selectMethod(specInfo.getReflection(), feature.getFeatureMethod().getReflection()))
      .collect(toCollection(LinkedHashSet::new));
  }

}
