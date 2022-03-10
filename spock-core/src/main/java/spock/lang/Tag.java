/*
 * Copyright 2022 the original author or authors.
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

package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TagExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a feature method or specification belongs to one or more user-defined categories.
 *
 * @since 2.2
 * @author Alexander Kriegisch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(TagExtension.class)
@Repeatable(Tag.Container.class)
public @interface Tag {
  /**
   * <b>Syntax rules:</b> Even though tags are general-purpose, user-defined semantic categories, one of those purposes
   * is that tags can also be mapped to JUnit platform tags, enabling users to filter their test classes and methods and
   * run only the ones they want in any given build. In order to ensure interoperability with the JUnit 5 platform,
   * please also obey the same syntax rules when naming tags, e.g. no whitespace inside tags ("my tag"), no commas,
   * parentheses, ampersands, vertical bars, exclamation points.
   * See <a href="https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-syntax-rules">
   *   JUnit syntax rules for tags</a> for more details.
   *
   * @return the user-defined categories which the annotated element belongs to
   */
  String[] value();

  /**
   * @since 2.2
   * @author Alexander Kriegisch
   */
  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    Tag[] value();
  }
}
