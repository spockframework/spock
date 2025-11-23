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

import org.spockframework.compiler.model.FilterBlock;
import org.spockframework.compiler.model.WhereBlock;
import org.spockframework.runtime.model.DataProcessorMetadata;
import org.spockframework.runtime.model.DataProviderMetadata;
import org.spockframework.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.*;
import org.objectweb.asm.Opcodes;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.spockframework.compiler.AstUtil.*;
import static org.spockframework.util.ExceptionUtil.sneakyThrow;
import static org.spockframework.util.Identifiers.COMBINED;

/**
 *
 * @author Peter Niederwieser
 */
public class WhereBlockRewriter {
  private final WhereBlock whereBlock;
  private final FilterBlock filterBlock;
  private final IRewriteResources resources;
  private final boolean defineErrorRethrower;
  private final InstanceFieldAccessChecker instanceFieldAccessChecker;
  private final ErrorRethrowerUsageDetector errorRethrowerUsageDetector;

  private int dataProviderCount = 0;
  private final List<VariableExpression> dataTableVars = new ArrayList<>();
  // parameters of the data processor method (one for each data provider)
  private final List<Parameter> dataProcessorParams = new ArrayList<>();
  // statements of the data processor method (one for each parameterization variable)
  private final List<Statement> dataProcessorStats = new ArrayList<>();
  // parameterization variables of the data processor method
  private final List<VariableExpression> dataProcessorVars = new ArrayList<>();
  private final List<VariableExpression> previousDataTableVariableAccesses = new ArrayList<>();
  private final List<String> multiplicationFactorDataVariables = new ArrayList<>();
  private final List<Expression> dataVariableMultiplications = new ArrayList<>();
  private int localVariableCount = 0;

  private WhereBlockRewriter(WhereBlock whereBlock, FilterBlock filterBlock, IRewriteResources resources, boolean defineErrorRethrower) {
    this.whereBlock = whereBlock;
    this.filterBlock = filterBlock;
    this.resources = resources;
    this.defineErrorRethrower = defineErrorRethrower;
    instanceFieldAccessChecker = new InstanceFieldAccessChecker(resources);
    errorRethrowerUsageDetector = defineErrorRethrower ? new ErrorRethrowerUsageDetector() : null;
  }

  public static void rewrite(WhereBlock block, FilterBlock filterBlock, IRewriteResources resources, boolean defineErrorRethrower) {
    new WhereBlockRewriter(block, filterBlock, resources, defineErrorRethrower).rewrite();
  }

  private void rewrite() {
    ListIterator<Statement> stats = whereBlock.getAst().listIterator();
    ConstructorCallExpression multiplier = null;
    List<String> multiplicationDataVariables = new ArrayList<>();
    while (stats.hasNext()) {
      if (combineWithNext(stats) && !multiplicationFactorDataVariables.isEmpty()) {
        // we are after a data provider that is multiplied with the next data provider
        // remember the data variables for the multiplication
        multiplicationDataVariables.addAll(multiplicationFactorDataVariables);
        if (multiplier == null) {
          // this is the first multiplication
          // multiplier is a simple factor
          multiplier = createDataVariableMultiplicationFactor(multiplicationFactorDataVariables);
        } else {
          // this is the second or further multiplication in row
          // multiplier is a multiplication of the previous multiplier with the simple factor
          multiplier = createDataVariableMultiplication(
            multiplicationDataVariables,
            multiplier,
            createDataVariableMultiplicationFactor(multiplicationFactorDataVariables));
        }
      }

      // forget state of the last data provider and remember the one of the next data provider
      previousDataTableVariableAccesses.clear();
      multiplicationFactorDataVariables.clear();

      try {
        rewriteWhereStat(stats);

        boolean combineWithNext = combineWithNext(stats);

        // forbid accessing data table variables of previous data tables within
        // multiplications, as it does not behave in an expected way
        if ((multiplier != null) || combineWithNext) {
          List<VariableExpression> illegalAccesses = previousDataTableVariableAccesses
            .stream()
            .filter(expr -> !multiplicationFactorDataVariables.contains(expr.getName()))
            .collect(toList());
          if (!illegalAccesses.isEmpty()) {
            illegalAccesses.forEach(illegalAccess ->
              resources.getErrorReporter().error(illegalAccess, "Data tables that are part of combinations must not access data variables of other tables"));
            continue;
          }
        }

        if ((multiplier != null) && !combineWithNext) {
          // this is the last factor of a multiplication chain
          // finish the multiplication and record it
          multiplicationDataVariables.addAll(multiplicationFactorDataVariables);
          dataVariableMultiplications.add(createDataVariableMultiplication(
            multiplicationDataVariables,
            multiplier,
            createDataVariableMultiplicationFactor(multiplicationFactorDataVariables)));

          // reset state to wait for the next multiplication
          multiplier = null;
          multiplicationDataVariables.clear();
        }
      } catch (InvalidSpecCompileException e) {
        resources.getErrorReporter().error(e);
      }
    }

    whereBlock.getAst().clear();
    handleFeatureParameters();
    createDataProcessorMethod();
    createDataVariableMultiplicationsMethod();
    createFilterMethod();
  }

  private static boolean isMultiplicand(Statement stat) {
    return COMBINED.equals(stat.getStatementLabel());
  }

  private boolean combineWithNext(ListIterator<Statement> stats) {
    if (!stats.hasNext()) {
      return false;
    }
    Statement next = stats.next();
    try {
      return isMultiplicand(next) || (isDataTableSeparator(next) && combineWithNext(stats));
    } finally {
      stats.previous();
    }
  }

  private ConstructorCallExpression createDataVariableMultiplicationFactor(List<String> dataVariables) {
    return new ConstructorCallExpression(
      resources.getAstNodeCache().DataVariableMultiplicationFactor,
      new ArgumentListExpression(new ArrayExpression(ClassHelper.STRING_TYPE, dataVariables.stream().map(ConstantExpression::new).collect(toList()))));
  }

  private ConstructorCallExpression createDataVariableMultiplication(List<String> dataVariables,
                                                                     Expression multiplier, Expression multiplicand) {
    return new ConstructorCallExpression(
      resources.getAstNodeCache().DataVariableMultiplication,
      new ArgumentListExpression(
        new ArrayExpression(ClassHelper.STRING_TYPE, dataVariables.stream().map(ConstantExpression::new).collect(toList())),
        multiplier, multiplicand));
  }

  private void rewriteWhereStat(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    Statement stat = stats.next();

    // binary expressions are potentially parameterizations
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr != null) {
      // don't allow subclasses like DeclarationExpression
      if (binExpr.getClass() != BinaryExpression.class) {
        throw notAParameterization(stat);
      }

      stats.previous();
      rewriteBinaryWhereStat(stats);
      return;
    }

    // reposition before current statement
    stats.previous();
    // search for semicolon separated data table header row
    List<Expression> potentialHeaderRow = getExpressionChain(stats);
    // rewind
    potentialHeaderRow
      .stream()
      .skip(1)
      .forEach(expression -> stats.previous());

    if (potentialHeaderRow.size() > 1) {
      stats.previous();
      rewriteExpressionTableLikeParameterization(stats);
      return;
    }

    // if statement is a data table separator (two or more underscores)
    // just ignore it, it is mainly meant to separate two consecutive
    // data tables, but is allowed anywhere in the where block, for
    // example to use it as top border for a data table like:
    // __________
    // x | y || z
    // 1 | 2 || 3
    // 4 | 5 || 6
    if (isDataTableSeparator(stat)) {
      // transplant statement labels like "combined"
      // to the following statement if present
      transplantStatementLabelsToNext(stats, stat);
      return;
    }

    // otherwise => not a parameterization
    throw notAParameterization(stat);
  }

  private static void transplantStatementLabelsToNext(ListIterator<Statement> stats, Statement source) {
    if (stats.hasNext()) {
      Statement next = stats.next();
      List<String> statementLabels = source.getStatementLabels();
      if (statementLabels != null) {
        statementLabels.forEach(next::addStatementLabel);
      }
      stats.previous();
    }
  }

  private void rewriteBinaryWhereStat(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    Statement stat = stats.next();
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    int type = binExpr.getOperation().getType();

    if (type == Types.LEFT_SHIFT) {
      // potentially a data pipe like:
      // x << [1, 2, 3]
      Expression leftExpr = binExpr.getLeftExpression();
      if (leftExpr instanceof VariableExpression) {
        // x << [1, 2, 3]
        rewriteSimpleParameterization(binExpr, stat, false);
      } else if (leftExpr instanceof ListExpression) {
        // [x, y, z] << [[1, 2, 3]]
        rewriteMultiParameterization(binExpr, stat);
      } else {
        // neither of the other two
        throw notAParameterization(stat);
      }
    } else if (type == Types.ASSIGN) {
      // potentially a derived data variable like:
      // y = 2 * x
      // or
      // (y, z) = [2, x]
      Expression leftExpr = binExpr.getLeftExpression();
      if (leftExpr instanceof VariableExpression) {
        // y = 2 * x
        rewriteSimpleDerivedParameterization(binExpr, stat);
      } else if (leftExpr instanceof TupleExpression) {
        // (y, z) = [2, x]
        rewriteMultiDerivedParameterization(binExpr, stat);
      } else {
        // neither of the other two
        throw notAParameterization(stat);
      }
      verifyDerivedDataVariableIsNotCombined(stats, stat);
    } else if (getOrExpression(binExpr) != null) {
      // header line of data table like:
      // x | y || z
      // 1 | 2 || 3
      // 4 | 5 || 6
      // push back header line
      stats.previous();
      // rewrite data table
      rewriteBinaryTableLikeParameterization(stats);
    } else {
      // binary expression is neither of type left-shift, nor assign and not a data table
      throw notAParameterization(stat);
    }
  }

  private void verifyDerivedDataVariableIsNotCombined(ListIterator<Statement> stats, Statement stat) throws InvalidSpecCompileException {
    // if derived data variable is multiplicand => error
    if (isMultiplicand(stat)) {
      throw derivedDataVariablesCannotBeCombined(stat);
    }

    // if derived data variable is multiplier => error
    int i = 0;
    try {
      while (stats.hasNext()) {
        Statement next = stats.next();
        i++;
        if (isMultiplicand(next)) {
          throw derivedDataVariablesCannotBeCombined(stat);
        }
        // if not data table separator, we are done
        // otherwise check next statement for combined label
        if (!isDataTableSeparator(next)) {
          break;
        }
      }
    } finally {
      for (int j = 0; j < i; j++) {
        stats.previous();
      }
    }
  }

  private List<Expression> getExpressionChain(ListIterator<Statement> stats) {
    List<Expression> result = new ArrayList<>();

    if (!stats.hasNext()) {
      return result;
    }

    Statement stat = stats.next();
    while (true) {
      Expression expr = getExpression(stat, Expression.class);
      if (expr == null) {
        stats.previous();
        break;
      }

      result.add(expr);

      if (!stats.hasNext()) {
        break;
      }

      Statement nextStat = stats.next();
      if (nextStat.getLineNumber() != stat.getLastLineNumber()) {
        // new data table row
        stats.previous();
        break;
      }
      if (nextStat.getStatementLabel() != null) {
        sneakyThrow(columnsInDataTableMustNotBeLabeled(nextStat));
      }
      stat = nextStat;
    }

    return result;
  }

  private void createDataProviderMethod(Expression dataProviderExpr, int nextDataVariableIndex, boolean addDataTableParameters) {
    instanceFieldAccessChecker.check(dataProviderExpr);

    List<Statement> dataProviderStats = new ArrayList<>();
    if (defineErrorRethrower && errorRethrowerUsageDetector.detectedErrorRethrowerUsage(dataProviderExpr)) {
      resources.getErrorRecorders().defineErrorRethrower(dataProviderStats);
    }

    ReturnStatement returnStat = new ReturnStatement(dataProviderExpr);
    returnStat.setSourcePosition(dataProviderExpr);
    dataProviderStats.add(returnStat);

    MethodNode method =
      new MethodNode(
        InternalIdentifiers.getDataProviderName(whereBlock.getParent().getAst().getName(), dataProviderCount++),
        Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
        ClassHelper.OBJECT_TYPE,
        // only add parameters when generating a provider method for a data table column
        addDataTableParameters ? createPreviousDataTableParameters(nextDataVariableIndex) : Parameter.EMPTY_ARRAY,
        ClassNode.EMPTY_ARRAY,
        new BlockStatement(dataProviderStats, null));

    method.addAnnotation(createDataProviderAnnotation(dataProviderExpr, nextDataVariableIndex, addDataTableParameters));
    whereBlock.getParent().getParent().getAst().addMethod(method);
  }

  private Parameter[] createPreviousDataTableParameters(int nextDataVariableIndex) {
    return getPreviousDataTableVariables(nextDataVariableIndex)
      .stream()
      .map(previousDataTableVariable -> new Parameter(
        ClassHelper.LIST_TYPE.getPlainNodeReference(),
        getDataTableParameterName(previousDataTableVariable)))
      .toArray(Parameter[]::new);
  }

  private List<String> getPreviousDataTableVariables(int nextDataVariableIndex) {
    List<String> results = new ArrayList<>(nextDataVariableIndex);
    for (int i = 0; i < nextDataVariableIndex; i++) {
      VariableExpression dataProcessorVar = dataProcessorVars.get(i);
      if (dataTableVars
        .stream()
        .map(VariableExpression::getName)
        .noneMatch(dataProcessorVar.getName()::equals)) {
        continue;
      }
      results.add(dataProcessorVar.getName());
    }
    return results;
  }

  private String getDataTableParameterName(String dataTableVariable) {
    return "$spock_p_" + dataTableVariable;
  }

  private AnnotationNode createDataProviderAnnotation(Expression dataProviderExpr, int nextDataVariableIndex,
                                                      boolean addDataTableParameters) {
    AnnotationNode ann = new AnnotationNode(resources.getAstNodeCache().DataProviderMetadata);

    ann.addMember(DataProviderMetadata.LINE, new ConstantExpression(dataProviderExpr.getLineNumber()));

    List<Expression> dataVariableNames = new ArrayList<>();
    for (int i = nextDataVariableIndex; i < dataProcessorVars.size(); i++)
      dataVariableNames.add(new ConstantExpression(dataProcessorVars.get(i).getName()));
    ann.addMember(DataProviderMetadata.DATA_VARIABLES, new ListExpression(dataVariableNames));

    if (addDataTableParameters) {
      ListExpression previousDataTableVariables = getPreviousDataTableVariables(nextDataVariableIndex)
        .stream()
        .map(ConstantExpression::new)
        .collect(collectingAndThen(
          Collectors.<Expression>toList(),
          ListExpression::new));
      ann.addMember(DataProviderMetadata.PREVIOUS_DATA_TABLE_VARIABLES, previousDataTableVariables);
    }

    return ann;
  }

  private Parameter createDataProcessorParameter() {
    Parameter p = new Parameter(ClassHelper.OBJECT_TYPE, "$spock_p" + dataProcessorParams.size());
    dataProcessorParams.add(p);
    return p;
  }

   //from: x << [1, 2, 3]
  // generates: arg = argMethodParam
  private void rewriteSimpleParameterization(BinaryExpression binExpr, ASTNode sourcePos, boolean addDataTableParameters)
      throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();
    Parameter dataProcessorParameter = createDataProcessorParameter();
    VariableExpression arg = (VariableExpression) binExpr.getLeftExpression();
    VariableExpression dataVar = createDataProcessorVariable(arg, sourcePos);
    createDataProcessorStatement(dataVar, new VariableExpression(dataProcessorParameter), sourcePos);
    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex, addDataTableParameters);
  }

  // from: [x, y, z] << [[1, 2, 3]]
  // generates:
  // arg0 = argMethodParam.getAt(0)
  // arg1 = argMethodParam.getAt(1)
  private void rewriteMultiParameterization(BinaryExpression binExpr, Statement enclosingStat)
      throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();
    Parameter dataProcessorParameter = createDataProcessorParameter();
    ListExpression list = (ListExpression) binExpr.getLeftExpression();
    rewriteMultiParameterization(list, new VariableExpression(dataProcessorParameter), enclosingStat);
    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex, false);
  }

  private void rewriteMultiParameterization(ListExpression list, Expression rightBase, Statement enclosingStat)
      throws InvalidSpecCompileException {
    List<Expression> listElems = list.getExpressions();
    for (int i = 0; i < listElems.size(); i++) {
      Expression listElem = listElems.get(i);
      if (AstUtil.isWildcardRef(listElem)) continue;

      if (listElem instanceof VariableExpression) {
        VariableExpression variable = createDataProcessorVariable(listElem, enclosingStat);
        createDataProcessorStatement(variable, createGetAtWithMapSupportMethodCall(rightBase, i, variable.getName()), enclosingStat);
      } else if (listElem instanceof ListExpression) {
        VariableExpression variable = new VariableExpression("$spock_l" + localVariableCount++);
        createDataProcessorStatement(variable, createGetAtMethodCall(rightBase, i), enclosingStat);
        rewriteMultiParameterization(((ListExpression) listElem), variable, enclosingStat);
      } else {
        throw notAParameterization(enclosingStat);
      }
    }
  }

  private void rewriteSimpleDerivedParameterization(BinaryExpression parameterization, Statement enclosingStat)
      throws InvalidSpecCompileException {
    VariableExpression dataVar = createDataProcessorVariable(parameterization.getLeftExpression(), enclosingStat);
    createDataProcessorStatement(dataVar, parameterization.getRightExpression(), enclosingStat);
  }

  private void rewriteMultiDerivedParameterization(BinaryExpression binExpr, Statement enclosingStat)
      throws InvalidSpecCompileException {
    TupleExpression tuple = (TupleExpression) binExpr.getLeftExpression();

    VariableExpression rightExpression = new VariableExpression("$spock_l" + localVariableCount++);
    createDataProcessorStatement(rightExpression, binExpr.getRightExpression(), enclosingStat);

    List<Expression> tupleElems = tuple.getExpressions();
    for (int i = 0; i < tupleElems.size(); i++) {
      Expression tupleElem = tupleElems.get(i);
      if (AstUtil.isWildcardRef(tupleElem)) continue;
      VariableExpression dataVar = createDataProcessorVariable(tupleElem, enclosingStat);
      createDataProcessorStatement(dataVar, createGetAtMethodCall(rightExpression, i), enclosingStat);
    }
  }

  private void createDataProcessorStatement(VariableExpression variable, Expression right, ASTNode sourcePos) {
    CastExpression castExpression = new CastExpression(variable.getType(), right);
    castExpression.setCoerce(true);

    ExpressionStatement exprStat =
      new ExpressionStatement(
        new DeclarationExpression(
          variable,
          Token.newSymbol(Types.ASSIGN, -1, -1),
          castExpression));
    exprStat.setSourcePosition(sourcePos);
    dataProcessorStats.add(exprStat);
  }

  private void rewriteBinaryTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    rewriteTableLikeParameterization(stats, result -> {
      Statement stat = stats.next();
      BinaryExpression orExpr = getOrExpression(stat);
      // if binary data table ends, i.e. next expression is not an `or` expression, stop processing
      if (orExpr == null) {
        stats.previous();
        return true;
      }
      splitRow(orExpr, result);
      return false;
    });
  }

  private void rewriteExpressionTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    rewriteTableLikeParameterization(stats, result -> {
      List<Expression> row = getExpressionChain(stats);
      // if expression data table ends, i.e. next expression is not part of a chain, stop processing
      if (row.size() <= 1) {
        // rewind
        row.forEach(expression -> stats.previous());
        return true;
      }
      result.addAll(row);
      return false;
    });
  }

  private void rewriteTableLikeParameterization(ListIterator<Statement> stats, Function<List<Expression>, Boolean> rowExtractor) throws InvalidSpecCompileException {
    LinkedList<List<Expression>> rows = new LinkedList<>();

    // iterate rows until where block ends
    while (stats.hasNext()) {
      if (rows.size() > 0) {
        // or the next statement is combined-labeled after the table has started already, i.e. a new data table
        // or data pipe starts that should be cross-multiplied so the current table is finished
        boolean nextIsMultiplicand = isMultiplicand(stats.next());
        stats.previous();
        if (nextIsMultiplicand) {
          break;
        }
      }

      List<Expression> row = new ArrayList<>();
      // or until the next statement is not a valid table row
      if (rowExtractor.apply(row)) {
        break;
      }

      // if there is already at least the header row
      // and the row to be added does not have the same size as previous rows
      // => compile error
      if (rows.size() > 0 && rows.getLast().size() != row.size())
        throw new InvalidSpecCompileException(row.get(0), String.format("Row in data table has wrong number of elements (%s instead of %s)", row.size(), rows.getLast().size()));

      // add the new row
      rows.add(row);
    }

    // if no more rows are present and the table header
    // had no following data rows => compile error
    if (rows.size() == 1) {
      throw new InvalidSpecCompileException(rows.get(0).get(0), "Data table must have more than just the header row");
    }

    // transform the table into a
    // series of simple data pipes
    for (List<Expression> column : transposeTable(rows))
      turnIntoSimpleParameterization(column);
  }

  List<List<Expression>> transposeTable(List<List<Expression>> rows) {
    List<List<Expression>> columns = new ArrayList<>();
    if (rows.isEmpty()) return columns;

    for (int i = 0; i < rows.get(0).size(); i++)
      columns.add(new ArrayList<>());

    for (List<Expression> row : rows)
      for (int i = 0; i < row.size(); i++)
        columns.get(i).add(row.get(i));

    return columns;
  }

  private void turnIntoSimpleParameterization(List<Expression> column) throws InvalidSpecCompileException {
    VariableExpression varExpr = ObjectUtil.asInstance(column.get(0), VariableExpression.class);
    if (varExpr == null)
      throw dataTableHeaderMayOnlyContainVariableNames(column.get(0));
    if (AstUtil.isWildcardRef(varExpr)) {
      // assertion: column has a wildcard header, but the method's
      // explicit parameter list does not have a wildcard parameter
      return; // ignore column (see https://github.com/spockframework/spock/pull/48/)
    }

    ListExpression listExpr = new ListExpression();

    // get the previous data table column variables
    List<String> previousVariables = getPreviousDataTableVariables(dataProcessorVars.size());
    if (previousVariables.isEmpty()) {
      // this is the first column,
      // simply add the cell expressions to the data provider statement:
      // x | y
      // 1 | x + 1
      // 2 | x + 2
      // 3 | x + 3
      // =>
      // [
      //   1,
      //   2,
      //   3
      // ]
      column.stream().skip(1).forEach(listExpr::addExpression);
    } else {
      // this is not the first column,
      // cell expressions might reference previous columns,
      // add statements that extract the correct row values for each expression,
      // but only if they actually reference the previous column
      // and wrap the whole thing in a closure and its call if necessary:
      // x | y
      // 1 | x + 1
      // 2 | x + 2
      // 3 | 3
      // =>
      // [
      //   {
      //     def x = $spock_p_x.get(0)
      //     return x + 1
      //   }(),
      //
      //   {
      //     def x = $spock_p_x.get(1)
      //     return x + 2
      //   }(),
      //
      //   3
      // ]
      for (int row = 0, rows = column.size() - 1; row < rows; row++) {
        Expression providerExpression = column.get(row + 1);
        providerExpression.visit(new DataProviderInternalsVerifier());

        List<VariableExpression> previousVariableAccesses = getReferencedPreviousVariables(previousVariables, providerExpression);
        previousDataTableVariableAccesses.addAll(previousVariableAccesses);

        // no previous variables referenced => just use the expression
        if (previousVariableAccesses.isEmpty()) {
          listExpr.addExpression(providerExpression);
          continue;
        }

        // otherwise generate the extractors and closure
        List<Statement> statements = new ArrayList<>();
        Set<String> referencedPreviousVariables = previousVariableAccesses.stream().map(VariableExpression::getName).collect(toSet());
        generatePreviousColumnExtractorStatements(referencedPreviousVariables, row, statements);
        ReturnStatement providerStatement = new ReturnStatement(providerExpression);
        providerStatement.setSourcePosition(providerExpression);
        statements.add(providerStatement);

        ClosureExpression closureExpression = new ClosureExpression(
          Parameter.EMPTY_ARRAY,
          new BlockStatement(statements, null));

        listExpr.addExpression(createDirectMethodCall(
          closureExpression,
          resources.getAstNodeCache().Closure_Call,
          ArgumentListExpression.EMPTY_ARGUMENTS));
      }
    }

    BinaryExpression binExpr = new BinaryExpression(varExpr, Token.newSymbol(Types.LEFT_SHIFT, -1, -1), listExpr);
    dataTableVars.add(new VariableExpression(varExpr.getName(), varExpr.getType()));
    // NOTE: varExpr may not be the "perfect" source position here, but as long as we rewrite data tables
    // into simple parameterizations, it seems like the best approximation; also this source position is
    // unlikely to make it into a compile error, because header variable has already been checked, and the
    // assignment itself is unlikely to cause a compile error. (It's more likely that the rval causes a
    // compile error, but the rval's source position is retained.)
    rewriteSimpleParameterization(binExpr, varExpr, true);
  }

  private void generatePreviousColumnExtractorStatements(Set<String> referencedPreviousVariables, int row,
                                                         List<Statement> statements) {
    for (String referencedPreviousVariable : referencedPreviousVariables) {
      statements.add(new ExpressionStatement(
        // def x = $spock_p_x.get(row)
        new DeclarationExpression(
          new VariableExpression(referencedPreviousVariable),
          Token.newSymbol(Types.ASSIGN, -1, -1),
          createDirectMethodCall(
            new VariableExpression(getDataTableParameterName(referencedPreviousVariable)),
            resources.getAstNodeCache().List_Get,
            new ConstantExpression(row)))));
    }
  }

  private List<VariableExpression> getReferencedPreviousVariables(List<String> previousVariables, Expression providerExpression) {
    return previousVariables
      .stream()
      .map(PreviousDataTableVariableUsageTracker::new)
      .peek(providerExpression::visit)
      .filter(PreviousDataTableVariableUsageTracker::hasFound)
      .flatMap(tracker -> tracker.getVariableExpressions().stream())
      .collect(toList());
  }

  private void splitRow(Expression row, List<Expression> parts) {
    BinaryExpression orExpr = getOrExpression(row);
    if (orExpr == null)
      parts.add(row);
    else {
      splitRow(orExpr.getLeftExpression(), parts);
      splitRow(orExpr.getRightExpression(), parts);
    }
  }

  private BinaryExpression getOrExpression(Statement stat) {
    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return getOrExpression(expr);
  }

  private BinaryExpression getOrExpression(Expression expr) {
    BinaryExpression binExpr = ObjectUtil.asInstance(expr, BinaryExpression.class);
    if (binExpr == null) return null;

    int binExprType = binExpr.getOperation().getType();
    if (binExprType == Types.BITWISE_OR || binExprType == Types.LOGICAL_OR) return binExpr;

    return null;
  }

  private VariableExpression createDataProcessorVariable(Expression varExpr, ASTNode sourcePos)
      throws InvalidSpecCompileException {
    if (!(varExpr instanceof VariableExpression))
      throw notAParameterization(sourcePos);

    VariableExpression typedVarExpr = (VariableExpression)varExpr;
    verifyDataProcessorVariable(typedVarExpr);

    // remember potential data variables for current multiplication factor
    multiplicationFactorDataVariables.add(typedVarExpr.getName());

    VariableExpression result = new VariableExpression(typedVarExpr.getName(), typedVarExpr.getType());
    dataProcessorVars.add(result);
    return result;
  }

  private void verifyDataProcessorVariable(VariableExpression varExpr) {
    Variable accessedVar = varExpr.getAccessedVariable();

    if (accessedVar instanceof VariableExpression) { // local variable
      resources.getErrorReporter().error(varExpr, "A variable named '%s' already exists in this scope", varExpr.getName());
      return;
    }

    if (isDataProcessorVariable(varExpr.getName())) {
      resources.getErrorReporter().error(varExpr, "Duplicate declaration of data variable '%s'", varExpr.getName());
    }
  }

  private boolean isDataProcessorVariable(String name) {
    for (VariableExpression var : dataProcessorVars)
      if (var.getName().equals(name))
        return true;
    return false;
  }

  private void handleFeatureParameters() {
    Parameter[] parameters = whereBlock.getParent().getAst().getParameters();
    Map<Boolean, List<Parameter>> declaredParameters = Arrays.stream(parameters).collect(
      partitioningBy(parameter -> isDataProcessorVariable(parameter.getName())));

    Map<String, Parameter> declaredDataVariableParameters = declaredParameters
      .get(TRUE)
      .stream()
      .collect(toMap(Parameter::getName, identity()));

    List<Parameter> auxiliaryParameters = declaredParameters.get(FALSE);

    List<Parameter> newParameters = new ArrayList<>(dataProcessorVars.size() + auxiliaryParameters.size());
    // first all data variables in order of where block
    for (VariableExpression dataProcessorVar : dataProcessorVars) {
      String name = dataProcessorVar.getName();
      Parameter declaredDataVariableParameter = declaredDataVariableParameters.get(name);
      newParameters.add(declaredDataVariableParameter == null
        ? new Parameter(ClassHelper.OBJECT_TYPE, name)
        : declaredDataVariableParameter);
    }
    // then all auxiliary parameters in declaration order
    newParameters.addAll(auxiliaryParameters);

    whereBlock.getParent().getAst().setParameters(newParameters.toArray(Parameter.EMPTY_ARRAY));
  }

  @SuppressWarnings("unchecked")
  private void createDataProcessorMethod() {
    if (dataProcessorVars.isEmpty()) return;

    instanceFieldAccessChecker.check(dataProcessorStats);

    if (defineErrorRethrower && errorRethrowerUsageDetector.detectedErrorRethrowerUsage(dataProcessorStats)) {
      resources.getErrorRecorders().defineErrorRethrower(dataProcessorStats);
    }

    dataProcessorStats.add(
        new ReturnStatement(
            new ArrayExpression(
                ClassHelper.OBJECT_TYPE,
                (List) dataProcessorVars)));

    BlockStatement blockStat = new BlockStatement(dataProcessorStats, null);

    MethodNode dataProcessorMethod = new MethodNode(
      InternalIdentifiers.getDataProcessorName(whereBlock.getParent().getAst().getName()),
      Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
      ClassHelper.OBJECT_TYPE,
      dataProcessorParams.toArray(Parameter.EMPTY_ARRAY),
      ClassNode.EMPTY_ARRAY,
      blockStat);
    dataProcessorMethod.addAnnotation(createDataProcessorAnnotation());

    whereBlock.getParent().getParent().getAst().addMethod(dataProcessorMethod);
  }

  private AnnotationNode createDataProcessorAnnotation() {
    AnnotationNode ann = new AnnotationNode(resources.getAstNodeCache().DataProcessorMetadata);
    ann.addMember(
      DataProcessorMetadata.DATA_VARIABLES,
      dataProcessorVars
        .stream()
        .map(VariableExpression::getName)
        .map(ConstantExpression::new)
        .collect(collectingAndThen(
          Collectors.<Expression>toList(),
          ListExpression::new))
      );
    return ann;
  }

  private void createDataVariableMultiplicationsMethod() {
    if (dataVariableMultiplications.isEmpty()) return;

    Statement[] returnStatement = {
      new ReturnStatement(
        new ArrayExpression(
          resources.getAstNodeCache().DataVariableMultiplication,
          dataVariableMultiplications))
    };

    BlockStatement blockStat = new BlockStatement(returnStatement, null);

    MethodNode dataVariableMultiplicationsMethod = new MethodNode(
      InternalIdentifiers.getDataVariableMultiplicationsName(whereBlock.getParent().getAst().getName()),
      Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
      resources.getAstNodeCache().DataVariableMultiplication.makeArray(),
      Parameter.EMPTY_ARRAY,
      ClassNode.EMPTY_ARRAY,
      blockStat);

    whereBlock.getParent().getParent().getAst().addMethod(dataVariableMultiplicationsMethod);
  }

  private void createFilterMethod() {
    if (dataProcessorVars.isEmpty() || (filterBlock == null)) return;

    DeepBlockRewriter deep = new DeepBlockRewriter(resources);
    deep.visit(filterBlock);

    List<Statement> filterStats = new ArrayList<>(filterBlock.getAst());
    filterBlock.getAst().clear();

    instanceFieldAccessChecker.check(filterStats);

    if (deep.isConditionFound()) {
      resources.getErrorRecorders().defineValueRecorder(filterStats);
    }
    if (deep.isDeepNonGroupedConditionFound()) {
      resources.getErrorRecorders().defineErrorRethrower(filterStats);
    }

    BlockStatement blockStat = new BlockStatement(filterStats, null);

    MethodNode filterMethod = new MethodNode(
      InternalIdentifiers.getFilterName(filterBlock.getParent().getAst().getName()),
      Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
      ClassHelper.VOID_TYPE,
      dataProcessorVars
        .stream()
        .map(variable -> new Parameter(ClassHelper.OBJECT_TYPE, variable.getName()))
        .toArray(Parameter[]::new),
      ClassNode.EMPTY_ARRAY,
      blockStat);

    filterBlock.getParent().getParent().getAst().addMethod(filterMethod);
  }

  private static InvalidSpecCompileException notAParameterization(ASTNode stat) {
    return new InvalidSpecCompileException(stat,
"where-blocks may only contain parameterizations (e.g. 'salary << [1000, 5000, 9000]; salaryk = salary / 1000')");
  }

  private static InvalidSpecCompileException columnsInDataTableMustNotBeLabeled(ASTNode stat) {
    return new InvalidSpecCompileException(stat, "Columns in data tables must not be labeled");
  }

  private static InvalidSpecCompileException derivedDataVariablesCannotBeCombined(ASTNode stat) {
    return new InvalidSpecCompileException(stat, "Derived data variables cannot be combined");
  }

  private static InvalidSpecCompileException dataTableHeaderMayOnlyContainVariableNames(ASTNode stat) {
    return new InvalidSpecCompileException(stat, "Header of data table may only contain variable names");
  }

  private static class DataProviderInternalsVerifier extends ClassCodeVisitorSupport {
    @Override
    protected SourceUnit getSourceUnit() {
      throw new UnsupportedOperationException("getSourceUnit");
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
      super.visitVariableExpression(expression);
      if (expression.getName().startsWith("$spock_p_")) {
        sneakyThrow(new InvalidSpecCompileException(expression, "You should not try to use Spock internals"));
      }
    }
  }

  private static class PreviousDataTableVariableUsageTracker extends ClassCodeVisitorSupport {
    private final String variable;
    private final List<VariableExpression> variableExpressions = new ArrayList<>();

    public PreviousDataTableVariableUsageTracker(String variable) {
      this.variable = variable;
    }

    boolean hasFound() {
      return !variableExpressions.isEmpty();
    }

    public List<VariableExpression> getVariableExpressions() {
      return variableExpressions;
    }

    @Override
    protected SourceUnit getSourceUnit() {
      throw new UnsupportedOperationException("getSourceUnit");
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
      super.visitVariableExpression(expression);
      if (((expression.getAccessedVariable() instanceof DynamicVariable)
          ||(expression.getAccessedVariable() instanceof Parameter))
          && expression.getName().equals(variable)) {
        variableExpressions.add(expression);
      }
    }
  }
}
