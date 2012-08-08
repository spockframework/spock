/*
 * Copyright 2012 the original author or authors.
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

package spock.mock;

import java.util.List;
import java.util.Map;

import org.spockframework.util.Nullable;

import spock.lang.Beta;

@Beta
public class MockConfiguration {
  private final String name;
  private final Class<?> type;
  private final MockNature nature;
  private final MockImplementation implementation;
  private final List<Object> constructorArgs;
  private final IMockInvocationResponder responder;
  private final boolean global;
  private final boolean verified;
  private final boolean useObjenesis;

  // TODO: should we lock down settings where stub and spy differ so that they can't be overwritten by options?
  @SuppressWarnings("unchecked")
  public MockConfiguration(@Nullable String name, Class<?> type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options) {
    this.name = name;
    this.type = type;
    this.nature = nature;
    this.implementation = implementation;
    this.constructorArgs = options.containsKey("constructorArgs") ?
        (List<Object>) options.get("constructorArgs") : null;
    this.responder = options.containsKey("responder") ?
        (IMockInvocationResponder) options.get("responder") : nature.getResponder();
    this.global = options.containsKey("global") ? (Boolean) options.get("global") : false;
    this.verified = options.containsKey("verified") ? (Boolean) options.get("verified") : nature.isVerified();
    this.useObjenesis = options.containsKey("useObjenesis") ? (Boolean) options.get("useObjenesis") : nature.isUseObjenesis();
  }

  @Nullable
  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public MockNature getNature() {
    return nature;
  }

  public MockImplementation getImplementation() {
    return implementation;
  }

  @Nullable
  public List<Object> getConstructorArgs() {
    return constructorArgs;
  }

  public IMockInvocationResponder getResponder() {
    return responder;
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isVerified() {
    return verified;
  }

  public boolean isUseObjenesis() {
    return useObjenesis;
  }
}
