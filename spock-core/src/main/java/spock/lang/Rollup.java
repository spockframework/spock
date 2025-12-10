/*
 * Copyright 2020 the original author or authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that iterations of a data-driven feature should not be made visible
 * as separate features to the outside world (IDEs, reports, etc.) but as one atomic test.
 * The {@code Rollup} annotation can also be put on a spec class. This has the same
 * effect as putting it on every data-driven feature method that is not already
 * annotated with {@code Rollup}.
 *
 * <p>Having {@code @Rollup} on a super spec does not influence the features of sub specs,
 * that is {@code @Rollup} is not inheritable.
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Rollup {
}
