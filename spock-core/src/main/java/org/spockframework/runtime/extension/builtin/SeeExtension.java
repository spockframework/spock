package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.Attachment;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecElementInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.See;

public class SeeExtension implements IStatelessAnnotationDrivenExtension<See> {
  @Override
  public void visitSpecAnnotation(See see, SpecInfo spec) {
    addAttachments(see, spec);
  }

  @Override
  public void visitFeatureAnnotation(See see, FeatureInfo feature) {
    addAttachments(see, feature);
  }

  private void addAttachments(See see, SpecElementInfo specElement) {
    for (String url : see.value()) {
      specElement.addAttachment(new Attachment(url, url));
    }
  }
}
