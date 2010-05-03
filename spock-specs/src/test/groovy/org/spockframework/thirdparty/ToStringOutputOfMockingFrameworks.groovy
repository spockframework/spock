
/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.thirdparty

import org.easymock.EasyMock
import org.mockito.Mockito
import org.jmock.Mockery

class ToStringOutputOfMockingFrameworks {
  static void main(args) {
    println "JMock:"
    Mockery context = new Mockery();
    def jmock1 = context.mock(Foo)
    println jmock1
    def jmock2 = context.mock(Foo, "explicitly named")
    println jmock2
    println()

    println "EasyMock:"
    def easymock1 = EasyMock.createMock(Foo)
    println easymock1
    def easymock2 = EasyMock.createMock("explicitlyNamed", Foo)
    println easymock2
    println()
    
    println "Mockito:"
    def mockito1 = Mockito.mock(Foo)
    println mockito1
    def mockito2 = Mockito.mock(Foo,"explicitly named")
    println mockito2
  }

}

interface Foo {}