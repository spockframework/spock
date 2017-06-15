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
import java.util.*;

//TODO: should not be generic
public abstract class SpecElementInfo<P extends NodeInfo, R extends AnnotatedElement>
    extends NodeInfo<P, R> implements ISkippable, IExcludable, ITaggable, IAttachmentContainer, IInterceptable {
  private boolean skipped = false;
  private boolean excluded = false;
  private final List<Tag> tags = new ArrayList<>();
  private final List<Attachment> attachments = new ArrayList<>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<>();

  @Override
  public boolean isSkipped() {
    return skipped;
  }

  @Override
  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
  }

  @Override
  public boolean isExcluded() {
    return excluded;
  }

  @Override
  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  @Override
  public List<Tag> getTags() {
    return tags;
  }

  @Override
  public void addTag(Tag tag) {
    tags.add(tag);
  }

  @Override
  public List<Attachment> getAttachments() {
    return attachments;
  }

  @Override
  public void addAttachment(Attachment attachment) {
    attachments.add(attachment);
  }

  @Override
  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  @Override
  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }
}
