/*
 *  Copyright 2024 the original author or authors.
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
 */

package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.BlockInfo;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.util.Beta;

/**
 * Listens to block events during the execution of a feature.
 * <p>
 * Usually used in conjunction with {@link org.spockframework.runtime.IRunListener}.
 * Currently, only extensions can register listeners.
 * They do so by invoking {@link org.spockframework.runtime.model.FeatureInfo#addBlockListener(IBlockListener)}.
 * It is preferred to use a single instance of this.
 * <p>
 * It is discouraged to perform long-running operations in the listener methods,
 * as they are called during the execution of the specification.
 * It is discouraged to perform any side effects affecting the tests.
 * <p>
 * When an exception is thrown in a block, the {@code blockExited} will not be called for that block.
 * If a cleanup block is present the cleanup block listener methods will still be called.
 *
 * @see org.spockframework.runtime.IRunListener
 * @author Leonard Brünings
 * @since 2.4
 */
@Beta
public interface IBlockListener {

  /**
   * Called when a block is entered.
   */
  default void blockEntered(IterationInfo iterationInfo, BlockInfo blockInfo) {}

  /**
   * Called when a block is exited.
   * <p>
   * This method is not called if an exception is thrown in the block.
   * The block that was active will be available in the {@link org.spockframework.runtime.model.IErrorContext}
   * and can be observed via {@link org.spockframework.runtime.IRunListener#error(ErrorInfo)}.
   */
  default void blockExited(IterationInfo iterationInfo, BlockInfo blockInfo) {}
}
