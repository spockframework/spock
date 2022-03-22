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

import java.util.List;

/**
 * A (spec) element that can be tagged.
 */
public interface ITaggable {
  /**
   * Returns the element's tags.
   *
   * @return the element's tags
   */
  List<Tag> getTags();

  /**
   * Returns the element's tags, optionally including parent tags.
   * <p>
   * The default implementation simply returns the result of {@link #getTags()} for backward compatibility. Overriding
   * classes are not required to remove any duplicates from the list, considering the fact that {@link Tag} currently
   * does not override {@link Object#equals(Object)}. Furthermore, the notion of what constitutes a duplicate - e.g.
   * duplicate key, duplicate key/value pair, duplicate name - might vary depending on the tag usage context.
   *
   * @param includeParents whether to recursively add parent tags to the result
   * @return the element's tags, optionally including parent tags
   * @since 2.2
   */
  default List<Tag> getTags(boolean includeParents) {
    return getTags();
  }

  /**
   * Adds a tag to the element.
   *
   * @param tag the tag to be added
   */
  void addTag(Tag tag);
}
