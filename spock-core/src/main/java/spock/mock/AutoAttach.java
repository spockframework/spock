/*
 * Copyright 2017 the original author or authors.
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
package spock.mock;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Automatically attaches detached mocks {@link DetachedMockFactory} to a Specification.
 *
 * This can be used if there is no explicit framework support for attaching detached mocks.
 * It does not work for {@code static} or {@code @Shared} fields.
 *
 * If you are using the Spring Framework, then use spock-spring extension instead.
 *
 * @since 1.2
 * @author Leonard Brünings
 */

@Beta
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(AutoAttachExtension.class)
public @interface AutoAttach {
}
