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

package spock.lang;

import java.lang.annotation.*;

/**
 * Indicates which type(s) are the subject(s) of a spec. If applied to a field, the
 * field's type is (part of) the SUS (subject under specification). If applied
 * to a spec class, the annotation's values are (part of) the SUS.
 * Currently, this annotation has only informational purposes.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Subject {
  /**
   * The types that are (part of) the spec's SUS. Only relevant if the annotation
   * is applied to a spec class.
   *
   * @return the types that are (part of) the spec's SUS
   */
  Class<?>[] value() default Void.class;
}