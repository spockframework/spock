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

package org.spockframework.runtime;

import org.spockframework.runtime.model.MethodMetadata;
import org.spockframework.runtime.model.SpeckMetadata;
import org.spockframework.runtime.model.BlockMetadata;
import org.spockframework.runtime.model.*;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
// cannot put @Speck on this class, because although Spock won't complain,
// JUnit doesn't like test w/o test methods
@SpeckMetadata
public class AnnotatedSpeckClass {
  @MethodMetadata(
    index = 0,
    name = "someFeature",
    kind = MethodKind.FEATURE,
    blocks = {
      @BlockMetadata(texts = "A call is made", kind = BlockKind.WHEN),
      @BlockMetadata(texts = "the phone rings", kind = BlockKind.THEN)
    }
  )
  public void someFeature() {}

  public void someFeature__dp0() {}

  public void someFeature__dp1() {}

  public void someFeature__ac() {}

  @MethodMetadata(index = 1, name = "anotherFeature", kind = MethodKind.FEATURE, blocks = {})
  private void anotherFeature() {}
}
