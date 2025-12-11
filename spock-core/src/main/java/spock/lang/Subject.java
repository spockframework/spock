/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.lang;

import java.lang.annotation.*;

/**
 * Indicates which objects/classes are the subjects of a specification. If applied
 * to a field, indicates that the field holds the subject of the specification.
 * If applied to a class, indicates that the classes listed as annotation
 * arguments are the subjects of the specification. Currently, this annotation
 * has only informational purposes.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Repeatable(Subject.Container.class)
public @interface Subject {
  /**
   * The classes which are the subjects of the specification. Irrelevant if the
   * annotation is applied to a field.
   *
   * @return the classes which are the subjects of the specification
   */
  Class<?>[] value() default Void.class;

  /**
   * @since 2.0
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
  @interface Container {
    Subject[] value();
  }
}
