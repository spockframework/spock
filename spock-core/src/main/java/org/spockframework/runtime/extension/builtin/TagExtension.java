/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spockframework.runtime.extension.builtin;

import org.junit.platform.engine.TestTag;
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecElementInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Tag;

/**
 * Processes {@link Tag @Tag} annotations on specifications and features, converting them to
 * {@link org.spockframework.runtime.model.Tag Spock tags} which can potentially be used by reporting extensions.
 * More importantly, it also makes the tag information available to JUnit 5 as
 * {@link org.junit.platform.engine.TestTag platform tags} in order to enable filtering, e.g. from build tools like
 * Maven or Gradle.
 *
 * @author Alexander Kriegisch
 * @see <a href="https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions">
 * JUnit platform tag expression syntax</a>
 * @since 2.2
 */
public class TagExtension implements IAnnotationDrivenExtension<Tag> {
  public static final String TEST_TAG_KEY = "testTag";

  @Override
  public void visitSpecAnnotation(Tag tag, SpecInfo spec) {
    addTags(tag, spec);
  }

  @Override
  public void visitFeatureAnnotation(Tag tag, FeatureInfo feature) {
    addTags(tag, feature);
  }

  private void addTags(Tag tag, SpecElementInfo<?, ?> specElement) {
    for (String value : tag.value()) {
      if (!TestTag.isValid(value))
        throw new InvalidTagNameException("Tag '" + value + "' must conform to JUnit platform tag syntax");
      specElement.addTag(new org.spockframework.runtime.model.Tag(value, TEST_TAG_KEY, value));
    }
  }

  public static class InvalidTagNameException extends ExtensionException {
    public InvalidTagNameException(String message) {
      super(message);
    }
  }
}
