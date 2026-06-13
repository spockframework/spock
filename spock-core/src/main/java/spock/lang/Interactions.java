/*
 * Copyright 2026 the original author or authors.
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

import org.spockframework.util.Beta;
import spock.mock.MockInteractionSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a helper method (written naturally, with mocks passed in as parameters)
 * whose body declares mock interactions. The Spock compiler synthesizes a
 * companion overload that takes the owning {@code Specification} as an injected
 * first parameter and routes interactions through it; calls from a spec with a
 * strongly-typed receiver are rewritten to invoke the companion. Mock creation
 * is not allowed in such methods (pass mocks in as parameters, or use
 * {@link MockInteractionSupport}).
 *
 * <p>Example:
 * <pre>
 * class OrderFixtures {
 *   {@literal @}Interactions
 *   void stubHappyPath(PaymentGateway gateway) {
 *     gateway.charge(_) &gt;&gt; Result.OK
 *     1 * gateway.audit(_)
 *   }
 * }
 * </pre>
 *
 * <p>Runtime retention is required so specs can detect calls to precompiled
 * annotated helpers in other compilation units (same rationale as
 * {@code @ConditionBlock}).
 *
 * @since 2.5
 */
@Beta
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interactions {}
