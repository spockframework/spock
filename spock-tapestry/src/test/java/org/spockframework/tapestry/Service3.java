/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.tapestry;

import org.apache.tapestry5.ioc.annotations.ServiceId;

@ServiceId("Service3")
public class Service3 implements IService3 {
  private final IService2 service2;

  public Service3(IService2 service2) {
    this.service2 = service2;
  }

  public String generateString() {
    return service2.generateQuickBrownFox();
  }
}
