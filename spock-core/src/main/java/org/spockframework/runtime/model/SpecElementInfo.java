/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.model;

import org.spockframework.runtime.extension.IMethodInterceptor;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

//TODO: should not be generic
public abstract class SpecElementInfo<P extends NodeInfo, R extends AnnotatedElement>
    extends NodeInfo<P, R> implements ISkippable, IExcludable, ITaggable, IAttachmentContainer, IInterceptable {
  private boolean skipped = false;
  private boolean excluded = false;
  private final List<Tag> tags = new ArrayList<Tag>();
  private final List<Attachment> attachments = new ArrayList<Attachment>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<IMethodInterceptor>();

  public boolean isSkipped() {
    return skipped;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
  }

  public boolean isExcluded() {
    return excluded;
  }

  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void addTag(Tag tag) {
    tags.add(tag);
  }

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public void addAttachment(Attachment attachment) {
    attachments.add(attachment);
  }

  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }
}
