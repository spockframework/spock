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

package org.spockframework.guice;

import spock.guice.UseModules;

import org.spockframework.runtime.intercept.AbstractDirectiveProcessor;
import org.spockframework.runtime.model.SpeckInfo;

public class GuiceProcessor extends AbstractDirectiveProcessor<UseModules> {
  @Override
  public void visitSpeckDirective(UseModules directive, SpeckInfo speck) {
    GuiceInterceptor interceptor = new GuiceInterceptor(speck, directive);
    speck.getSetupSpeckMethod().addInterceptor(interceptor);
    speck.getSetupMethod().addInterceptor(interceptor);
  }
}
