/*
 * Copyright 2009 the original author or authors.
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

package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.IssueExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Indicates that a feature method or specification relates to one or more
 * issues in an external issue tracking system.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(IssueExtension.class)
@Repeatable(Issue.Container.class)
public @interface Issue {
  /**
   * The IDs of the issues that the annotated element relates to.
   *
   * @return the IDs of the issues that the annotated element relates to
   */
  String[] value();

  /**
   * @since 2.0
   */
  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    Issue[] value();
  }
}
