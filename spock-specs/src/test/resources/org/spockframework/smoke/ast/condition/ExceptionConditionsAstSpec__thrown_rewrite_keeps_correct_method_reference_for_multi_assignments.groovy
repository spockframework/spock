/*
 * Copyright 2023 the original author or authors.
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
 *
 */

package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    def (java.lang.Object foobar, java.lang.Object b) = [null, null]
    this.getSpecificationContext().setThrownException(null)
    try {
        (foobar, b) = this.foobar()
    }
    catch (java.lang.Throwable $spock_ex) {
        this.getSpecificationContext().setThrownException($spock_ex)
    }
    finally {
    }
    this.thrownImpl(null, null, java.lang.IllegalStateException)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
}
