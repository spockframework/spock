/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

/**
 * Expresses a fact about a spec element. Mainly intended for reporting purposes.
 * May carry additional data in form of a key-value pair, and may link to an
 * external resource. Child elements inherit their parents' tags.
 */
public class Tag {
  private final String name;
  private final String key;
  private final Object value;
  private final String url;

  public Tag(String name) {
    this(name, null, null, null);
  }

  public Tag(String name, String url) {
    this(name, null, null, url);
  }

  public Tag(String name, String key, Object value) {
    this(name, key, value, null);
  }

  public Tag(String name, @Nullable String key, @Nullable Object value, @Nullable String url) {
    this.name = name;
    this.key = key;
    this.value = value;
    this.url = url;
  }

  /**
   * Returns the display name of the tag.
   *
   * @return the display name of the tag
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the filter key of the tag. Tags with the same key are
   * mutually exclusive; a spec element cannot have two tags with
   * the same key. A tag on a child element overrides any tag with
   * the same key on a parent element.
   *
   * The filter key may be {@code null}, in which case the tag
   * will not be amenable to filtering.
   *
   * @return the filter key of the tag
   */
  @Nullable
  public String getKey() {
    return key;
  }

  /**
   * Returns the filter value of the tag. The value is either a
   * string, or a number, or {@code null} if the tag doesn't have a
   * value.
   *
   * @return the filter value of the tag
   */
  @Nullable
  public Object getValue() {
    return value;
  }

  /**
   * Returns the URL associated with the tag, if any.
   *
   * @return the URL associated with the tag, if any
   */
  @Nullable
  public String getUrl() {
    return url;
  }

  /**
   * Tells whether the tag is filterable.
   *
   * @return whether the tag is filterable
   */
  public boolean isFilterable() {
    return key != null;
  }
}
