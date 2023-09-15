/*
 * Copyright 2023 the original author or authors.
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

import org.spockframework.runtime.extension.builtin.OrderExtension;
import org.spockframework.runtime.extension.builtin.orderer.AnnotatationBasedSpecOrderer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns an execution order to a specification or feature.
 * <p>
 * Annotations of this type are picked up by the {@link OrderExtension}, if and only if the
 * {@link AnnotatationBasedSpecOrderer} is activated in the Spock configuration file: <pre>
 * runner {
 *   orderer new AnnotatationBasedSpecOrderer()
 * }</pre>
 * <p>
 * See the Spock user manual for more details.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Order {
  int value();
}
