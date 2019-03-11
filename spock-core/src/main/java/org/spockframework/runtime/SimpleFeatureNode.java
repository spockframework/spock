package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class SimpleFeatureNode extends FeatureNode {
  public SimpleFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(),  MethodSource.from(featureInfo.getFeatureMethod().getReflection()), featureInfo);
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return context.withCurrentFeature(featureInfo); //.withParentId(getUniqueId())
  }
}
