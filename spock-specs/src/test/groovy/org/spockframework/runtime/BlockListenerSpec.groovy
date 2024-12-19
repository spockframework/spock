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

package org.spockframework.runtime

import org.spockframework.runtime.extension.IBlockListener
import org.spockframework.runtime.model.BlockInfo
import org.spockframework.runtime.model.BlockKind
import org.spockframework.runtime.model.IterationInfo
import spock.lang.Specification

class BlockListenerSpec extends Specification {

  List<BlockInfo> blocks = []
  List<BlockInfo> exitBlocks = []

  def setup() {
    specificationContext.currentIteration.feature.addBlockListener([
      blockEntered: { IterationInfo i, BlockInfo b ->
        blocks << b
      },
      blockExited: { IterationInfo i, BlockInfo b ->
        exitBlocks << b
      }] as IBlockListener)
  }

  def "BlockListener is called for each Block with text"() {
    given: "setup"
    expect: "precondition"
    when: "action"
    then: "assertion"

    cleanup: "cleanup"
    assert blocks.kind == [BlockKind.SETUP, BlockKind.EXPECT, BlockKind.WHEN, BlockKind.THEN, BlockKind.CLEANUP]
    assert blocks.texts == [["setup"], ["precondition"], ["action"], ["assertion"], ["cleanup"]]
    assert exitBlocks.kind == [BlockKind.SETUP, BlockKind.EXPECT, BlockKind.WHEN, BlockKind.THEN]
  }

  def "SpecificationContext holds a reference to the current block"() {
    assert specificationContext.currentBlock == null
    given: "setup"
    assert specificationContext.currentBlock.kind == BlockKind.SETUP
    expect: "precondition"
    specificationContext.currentBlock.kind == BlockKind.EXPECT
    when: "action"
    assert specificationContext.currentBlock.kind == BlockKind.WHEN
    then: "assertion"
    specificationContext.currentBlock.kind == BlockKind.THEN

    cleanup: "cleanup"
    assert specificationContext.currentBlock.kind == BlockKind.CLEANUP
  }

  def "blocks extended with and: are treated as singular block with multiple texts"() {
    given: "setup"
    and: "setup2"
    expect: "precondition"
    and: "precondition2"
    when: "action"
    and: "action2"
    then: "assertion"
    and: "assertion2"

    cleanup: "cleanup"
    assert blocks.kind == [BlockKind.SETUP, BlockKind.EXPECT, BlockKind.WHEN, BlockKind.THEN, BlockKind.CLEANUP]
    and: "cleanup2"
    assert blocks.texts == [["setup", "setup2"], ["precondition", "precondition2"], ["action", "action2"], ["assertion", "assertion2"], ["cleanup", "cleanup2"]]
    assert exitBlocks.kind == [BlockKind.SETUP, BlockKind.EXPECT, BlockKind.WHEN, BlockKind.THEN]
  }
}
