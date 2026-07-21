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

package org.spockframework.compiler;

import org.spockframework.compiler.model.*;

import java.util.List;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Splits the statements of a {@link Method} into {@link Block}s by their statement labels and
 * validates the block grammar via {@link BlockParseInfo} successors.
 * <p>
 * Extracted from {@link SpecParser} so that both feature methods and standalone
 * {@code @DataProvider} methods (see {@link DataProviderMethodRewriter}) share the same block
 * detection, label handling and grammar validation.
 */
final class BlockParser {
  private BlockParser() {}

  static void parseBlocks(Method method) throws InvalidSpecCompileException {
    List<Statement> stats = AstUtil.getStatements(method.getAst());
    Block currBlock = method.addBlock(new AnonymousBlock(method));

    String statementLabelToTransplant = null;
    for (Statement stat : stats) {
      if (stat.getStatementLabel() == null) {
        if (statementLabelToTransplant != null) {
          stat.setStatementLabel(statementLabelToTransplant);
        }
        currBlock.getAst().add(stat);
      } else {
        currBlock = addBlock(method, stat);
      }
      // Usually, you have a label on a statement like
      //
      // combined:
      //   x << [1]
      //
      // and the label stays on that statement and could be used in later stages of the AST processing.
      // Especially for "combined" this is essential for proper operation.
      //
      // But if the label has a description like
      //
      // combined: 'combined with x'
      //   x << [1]
      //
      // then the description is added as "text" to the current block and the whole statement is swallowed.
      // If the label is needed for further processing like for "combined", this is then missing,
      // so transplant the label to the following statement which it actually affects.
      statementLabelToTransplant = (getDescription(stat) == null) ? null : stat.getStatementLabel();
    }

    checkIsValidSuccessor(method, BlockParseInfo.METHOD_END,
        method.getAst().getLastLineNumber(), method.getAst().getLastColumnNumber());

    // set the block metadata index for each block this must be equal to the index of the block in the @BlockMetadata annotation
    int i = -1;
    for (Block block : method.getBlocks()) {
      if(!block.hasBlockMetadata()) continue;
      block.setBlockMetaDataIndex(++i);
    }
    // now that statements have been copied to blocks, the original statement
    // list is cleared; statements will be copied back after rewriting is done
    stats.clear();
  }

  private static Block addBlock(Method method, Statement stat) throws InvalidSpecCompileException {
    String label = stat.getStatementLabel();

    for (BlockParseInfo blockInfo: BlockParseInfo.values()) {
      if (!label.equals(blockInfo.toString())) continue;

      checkIsValidSuccessor(method, blockInfo, stat.getLineNumber(), stat.getColumnNumber());
      Block block = blockInfo.addNewBlock(method);
      String description = getDescription(stat);
      if (description == null)
        block.getAst().add(stat);
      else
        block.getDescriptions().add(description);

      return block;
    }

    throw new InvalidSpecCompileException(stat, "Unrecognized block label: " + label);
  }

  private static String getDescription(Statement stat) {
    ConstantExpression constExpr = AstUtil.getExpression(stat, ConstantExpression.class);
    return constExpr == null || !(constExpr.getValue() instanceof String) ?
        null : (String)constExpr.getValue();
  }

  private static void checkIsValidSuccessor(Method method, BlockParseInfo blockInfo, int line, int column)
      throws InvalidSpecCompileException {
    BlockParseInfo oldBlockInfo = method.getLastBlock().getParseInfo();
    if (!oldBlockInfo.getSuccessors(method).contains(blockInfo))
      throw new InvalidSpecCompileException(line, column, "'%s' is not allowed here; instead, use one of: %s",
          blockInfo, oldBlockInfo.getSuccessors(method), method.getName(), oldBlockInfo, blockInfo);
  }
}
