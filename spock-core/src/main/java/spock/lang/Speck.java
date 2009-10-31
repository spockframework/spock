/*
 * Copyright 2009 the original author or authors.
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

import java.lang.annotation.*;

/**
 * Marks a class as a Spock specification.
 *
 * @deprecated Extend from spock.lang.Specification instead. This class will
 * be removed in Spock 0.4.
 * @author Peter Niederwieser
 */

@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Speck {
  /**
   * The types whose behavior is described by this specification.
   *
   * @return the types whose behavior is described by this specification
   */
  Class<?>[] value() default Void.class;
}
