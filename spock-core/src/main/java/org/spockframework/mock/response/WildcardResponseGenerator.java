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

package org.spockframework.mock.response;

import org.spockframework.mock.*;
import org.spockframework.util.ThreadSafe;

/**
 * Returns a value using {@code Stub} semantics (see  {@link EmptyOrDummyResponse}).
 *
 *  The behavior was changed in Spock 2.0, before it would return the default response
 *  of the MockObject, making it effectively a redundant declaration.
 *
 * @author Peter Niederwieser
 * @author Leonard Brünings
 */
@ThreadSafe
public class WildcardResponseGenerator extends SingleResponseGenerator {
  @Override
  public Object doRespond(IMockInvocation invocation) {
    return EmptyOrDummyResponse.INSTANCE.respond(invocation);
  }
}
