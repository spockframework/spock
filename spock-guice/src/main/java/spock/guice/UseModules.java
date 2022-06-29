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

package spock.guice;

import org.spockframework.guice.GuiceExtension;
import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.*;

import com.google.inject.Module;
import org.spockframework.util.Beta;

/**
 * Activates <a href="https://code.google.com/p/google-guice/">Guice</a> integration for a specification.
 * The specified modules will be started before and stopped after the specification's execution.
 * Services will be injected into the specification based on regular Guice annotations.
 *
 * Detached mocks/stubs created by the {@link spock.mock.DetachedMockFactory} are automatically attached
 * to the Specification when they are injected via {@code @Inject}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(GuiceExtension.class)
@Repeatable(UseModules.Container.class)
public @interface UseModules {
  Class<? extends Module>[] value();

  /**
   * @since 2.0
   */
  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Container {
    UseModules[] value();
  }
}
