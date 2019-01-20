package org.spockframework.runtime;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.spockframework.runtime.model.SpecInfo;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.singleton;
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
  public Set<Class<? extends DiscoverySelector>> getSupportedSelectorTypes() {
    return singleton(ClassSelector.class);
  }

  @Override
  public Optional<Result> resolveSelector(DiscoverySelector selector, Context context) {
    if (selector instanceof ClassSelector) {
      return resolveClass(((ClassSelector) selector).getJavaClass(), context);
    }
    return Optional.empty();
  }

  @Override
  public Optional<Result> resolveUniqueId(UniqueId uniqueId, Context context) {
    UniqueId.Segment lastSegment = uniqueId.getLastSegment();
    if ("spec".equals(lastSegment.getType())) {
      String className = lastSegment.getValue();
      Class<?> specClass = ReflectionSupport.tryToLoadClass(className).getOrThrow(SpockException::new);
      return resolveClass(specClass, context);
    }
    return Optional.empty();
  }

  private Optional<Result> resolveClass(Class<?> specClass, Context context) {
    if (SpecUtil.isRunnableSpec(specClass) && classNameFilter.test(specClass.getName())) {
      SpecInfo specInfo = new SpecInfoBuilder(specClass).build();
      return context
        .addToParent(parent -> {
          UniqueId uniqueId = parent.getUniqueId().append("spec", specInfo.getReflection().getName());
          runContext.createExtensionRunner(specInfo).run();
          return Optional.of(new SpecNode(uniqueId, specInfo));
        })
        .map(specNode -> toResult(specInfo, specNode));
    }
    return Optional.empty();
  }

  private Result toResult(SpecInfo specInfo, SpecNode specNode) {
    return Result.of(Match.of(specNode, features(specInfo)));
  }

  private Supplier<Set<? extends DiscoverySelector>> features(SpecInfo specInfo) {
    return () -> specInfo.getAllFeaturesInExecutionOrder().stream()
      .map(feature -> selectMethod(specInfo.getReflection(), feature.getFeatureMethod().getReflection()))
      .collect(toCollection(LinkedHashSet::new));
  }

}
