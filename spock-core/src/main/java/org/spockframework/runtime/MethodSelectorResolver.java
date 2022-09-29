package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.spockframework.runtime.model.IterationFilter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class MethodSelectorResolver implements SelectorResolver {

  @Override
  public Resolution resolve(MethodSelector selector, Context context) {

    String methodName = selector.getMethodName();
    Predicate<FeatureInfo> filter = feature ->
      reflectionNameOrMethodNameEquals(methodName, feature);

    DiscoverySelector parentSelector = selectClass(selector.getJavaClass());
    return resolveAllowingAllIndexes(context, parentSelector, filter);
  }

  @Override
  public Resolution resolve(UniqueIdSelector selector, Context context) {
    UniqueId uniqueId = selector.getUniqueId();
    UniqueId.Segment lastSegment = uniqueId.getLastSegment();
    if ("feature".equals(lastSegment.getType())) {
      String methodName = lastSegment.getValue();
      return resolveAllowingAllIndexes(
        context,
        selectUniqueId(uniqueId.removeLastSegment()),
        feature -> methodName.equals(feature.getFeatureMethod().getReflection().getName())
      );
    }
    if ("iteration".equals(lastSegment.getType())) {
      int index = Integer.parseInt(lastSegment.getValue());
      UniqueId featureMethodUniqueId = uniqueId.removeLastSegment();
      String methodName = featureMethodUniqueId.getLastSegment().getValue();
      return resolveWithIterationFilter(
        context,
        selectUniqueId(featureMethodUniqueId.removeLastSegment()),
        feature -> methodName.equals(feature.getFeatureMethod().getReflection().getName()),
        iterationFilter -> iterationFilter.allow(index)
      );
    }
    return Resolution.unresolved();
  }

  @Override
  public Resolution resolve(IterationSelector selector, Context context) {
    if (selector.getParentSelector() instanceof MethodSelector) {
      MethodSelector methodSelector = (MethodSelector) selector.getParentSelector();

      return resolveWithIterationFilter(
        context,
        selectClass(methodSelector.getJavaClass()),
        feature -> reflectionNameOrMethodNameEquals(methodSelector.getMethodName(), feature),
        iterationFilter -> selector.getIterationIndices().forEach(iterationFilter::allow)
      );
    }
    return SelectorResolver.super.resolve(selector, context);
  }

  private Resolution resolveAllowingAllIndexes(Context context, DiscoverySelector parentSelector, Predicate<FeatureInfo> featureFilter) {
    return resolveWithIterationFilter(context, parentSelector, featureFilter, IterationFilter::allowAll);
  }

  private Resolution resolveWithIterationFilter(Context context, DiscoverySelector parentSelector, Predicate<FeatureInfo> featureFilter, Consumer<IterationFilter> iterationFilterAdjuster) {
    return resolve(context,
      parentSelector,
      featureFilter,
      testDescriptor -> {
        ((SpecNode) testDescriptor).getNodeInfo().getAllFeatures().stream()
          .filter(featureFilter)
          .findAny()
          .map(FeatureInfo::getIterationFilter)
          .ifPresent(iterationFilterAdjuster);
        return Match.partial(testDescriptor);
      }
    );
  }

  private Resolution resolve(Context context, DiscoverySelector parentSelector, Predicate<FeatureInfo> filter, Function<TestDescriptor, Match> matchCreator) {
    return context.resolve(parentSelector)
      .map(testDescriptor -> handle(testDescriptor, filter))
      .map(descriptor -> Resolution.match(matchCreator.apply(descriptor)))
      .orElseGet(Resolution::unresolved);
  }

  private TestDescriptor handle(TestDescriptor testDescriptor, Predicate<FeatureInfo> filter) {
    if (testDescriptor instanceof SpecNode) {
      SpecNode specNode = (SpecNode) testDescriptor;
      long count = specNode.getNodeInfo().getAllFeaturesInExecutionOrder().stream()
        .filter(filter)
        .peek(featureInfo -> featureInfo.setExcluded(false))
        .count();
      return count == 0 ? null : testDescriptor;
    }
    return null;
  }

  private static boolean reflectionNameOrMethodNameEquals(String methodName, FeatureInfo feature) {
    return feature.getFeatureMethod().getReflection().getName().equals(methodName)
      || methodName.equals(feature.getName());
  }
}
