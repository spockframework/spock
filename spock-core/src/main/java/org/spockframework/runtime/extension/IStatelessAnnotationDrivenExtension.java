/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.runtime.extension;

import java.lang.annotation.Annotation;

import org.spockframework.util.Beta;

/**
 * A variant of the {@link IAnnotationDrivenExtension} that is stateless and does not create a new instance for each Specification.
 * <p>
 * Use this interface if the extension does not need to store any use specific state.
 * Injecting a configuration object is still allowed.
 * You can use the {@link IStore} to store state that should be shared.
 * <p>
 * Implementations of this interface must be thread-safe.
 * @since 2.4
 * @see ExtensionAnnotation
 * @see spock.config.ConfigurationObject
 * @see IStore
 */
@Beta
public interface IStatelessAnnotationDrivenExtension<T extends Annotation> extends IAnnotationDrivenExtension<T> {
}
