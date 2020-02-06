package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import java.util.function.Predicate;

import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class MethodSelectorResolver implements SelectorResolver {

  @Override
  public Resolution resolve(MethodSelector selector, Context context) {

    String methodName = selector.getMethodName();
    Predicate<FeatureInfo> filter = feature ->
      feature.getFeatureMethod().getReflection().getName().equals(methodName)
        || methodName.equals(feature.getName());

    DiscoverySelector parentSelector = selectClass(selector.getJavaClass());
    return resolve(context, parentSelector, filter);
  }

  Resolution resolve(Context context, DiscoverySelector parentSelector, Predicate<FeatureInfo> filter) {
    return context.resolve(parentSelector).map(testDescriptor -> handle(testDescriptor, filter))
      .map(descriptor -> Resolution.match(Match.partial(descriptor)))
      .orElseGet(Resolution::unresolved);
  }

  private TestDescriptor handle(TestDescriptor testDescriptor, Predicate<FeatureInfo> filter) {
    if (testDescriptor instanceof SpecNode) {
      SpecNode specNode = (SpecNode) testDescriptor;
      long count = specNode.getSpecInfo().getAllFeaturesInExecutionOrder().stream()
        .filter(filter)
        .peek(featureInfo -> featureInfo.setExcluded(false))
        .count();
      return count == 0 ? null : testDescriptor;
    }
    return null;
  }

  @Override
  public Resolution resolve(UniqueIdSelector selector, Context context) {
    UniqueId uniqueId = selector.getUniqueId();
    UniqueId.Segment lastSegment = uniqueId.getLastSegment();
    if ("feature".equals(lastSegment.getType())) {
      String methodName = lastSegment.getValue();
      return resolve(context,
        selectUniqueId(uniqueId.removeLastSegment()),
        feature -> methodName.equals(feature.getFeatureMethod().getReflection().getName()));
    }
    if ("iteration".equals(lastSegment.getType())) {
      return resolve(selectUniqueId(uniqueId.removeLastSegment()), context);
//          if (parent instanceof ParameterizedFeatureNode) {
//            FeatureNode featureNode = (FeatureNode) parent;
//            int iterationIndex = Integer.parseInt(lastSegment.getValue());
//            // TODO Add iterationIndex as allowed index to featureNode
//          }
    }
    return Resolution.unresolved();
  }
}
