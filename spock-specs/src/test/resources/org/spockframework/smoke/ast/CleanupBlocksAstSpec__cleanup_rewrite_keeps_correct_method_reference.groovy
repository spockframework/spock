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
    return 'foo'
}

public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Object foobar
    java.lang.Throwable $spock_feature_throwable
    try {
        foobar = this.foobar()
        try {
            org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'println(foobar)', 6, 3, null, this, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), 'println'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), foobar)}, $spock_valueRecorder.realizeNas(4, false), false, 3)
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'println(foobar)', 6, 3, null, $spock_condition_throwable)}
        finally {
        }
    }
    catch (java.lang.Throwable $spock_tmp_throwable) {
        $spock_feature_throwable = $spock_tmp_throwable
        throw $spock_tmp_throwable
    }
    finally {
        try {
            foobar.size()
        }
        catch (java.lang.Throwable $spock_tmp_throwable) {
            if ( $spock_feature_throwable != null) {
                $spock_feature_throwable.addSuppressed($spock_tmp_throwable)
            } else {
                throw $spock_tmp_throwable
            }
        }
        finally {
        }
    }
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
}
