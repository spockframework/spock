package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.TestTag;
import spock.lang.Tag;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TagExtension implements IStatelessAnnotationDrivenExtension<Tag> {
  @Override
  public void visitSpecAnnotations(List<Tag> annotations, SpecInfo spec) {
    List<TestTag> tags = toTestTags(annotations);
    spec.getBottomSpec().getAllFeatures().forEach(f -> tags.forEach(f::addTestTag));
  }

  @Override
  public void visitFeatureAnnotations(List<Tag> annotations, FeatureInfo feature) {
    toTestTags(annotations).forEach(feature::addTestTag);
  }

  private List<TestTag> toTestTags(List<Tag> annotations) {
    return annotations.stream().map(Tag::value).map(TestTag::create).collect(toList());
  }
}
