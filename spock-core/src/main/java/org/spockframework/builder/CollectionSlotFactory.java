/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.builder;

import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.MopUtil;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

import groovy.lang.MetaProperty;

public class CollectionSlotFactory implements ISlotFactory {
  private static final Pattern pluralIESPattern = Pattern.compile(".*[^aeiouy]y", Pattern.CASE_INSENSITIVE);

  @Override
  public ISlot create(Object owner, Type ownerType, String name) {
    String plural = toPluralForm(name);
    MetaProperty property = GroovyRuntimeUtil.getMetaClass(owner).getMetaProperty(plural);
    return property != null && Collection.class.isAssignableFrom(property.getType()) && MopUtil.isReadable(property) ?
      new CollectionSlot(plural, owner, ownerType, property) : null;
  }

  private String toPluralForm(String word) {
    if (word.toLowerCase(Locale.ROOT).endsWith("s")) return word + "es";
    boolean matchesIESRule = pluralIESPattern.matcher(word).matches();
    return matchesIESRule ? word.substring(0, word.length() - 1) + "ies" : word + "s";
  }
}
