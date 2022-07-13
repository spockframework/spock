/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.spring.mock.imported

import org.spockframework.spring.mock.imported.example.*

import org.springframework.context.annotation.*

/**
 * Config for {@link SpringSpyOnTestFieldForExistingBeanIntegrationTests} and
 * {@link SpringSpyOnTestFieldForExistingBeanCacheIntegrationTests}. Extracted to a shared
 * config to trigger caching.
 *
 * original author Phillip Webb
 * @author Leonard Brünings
 */
@Configuration
@Import([ExampleServiceCaller, SimpleExampleService])
class SpringSpyOnTestFieldForExistingBeanConfig {

}
