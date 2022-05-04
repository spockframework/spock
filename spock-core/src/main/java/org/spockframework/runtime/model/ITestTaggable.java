package org.spockframework.runtime.model;

import java.util.Set;

public interface ITestTaggable {
  void addTestTag(TestTag tag);

  default void addTestTag(String tagName) {
    addTestTag(TestTag.create(tagName));
  }

  Set<TestTag> getTestTags();
}
