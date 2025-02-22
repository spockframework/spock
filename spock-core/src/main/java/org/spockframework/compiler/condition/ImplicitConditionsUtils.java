package org.spockframework.compiler.condition;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;
import org.spockframework.compiler.AstUtil;
import org.spockframework.compiler.ErrorReporter;

public class ImplicitConditionsUtils {

  private ImplicitConditionsUtils() {
  }

  // assumes that it is not an interaction
  public static boolean isImplicitCondition(Statement stat) {
    return stat instanceof ExpressionStatement
      && !(((ExpressionStatement) stat).getExpression() instanceof DeclarationExpression);
  }

  public static void checkIsValidImplicitCondition(Statement stat, ErrorReporter errorReporter) {
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null) return;

    if (Types.ofType(binExpr.getOperation().getType(), Types.ASSIGNMENT_OPERATOR)) {
      errorReporter.error(stat, "Expected a condition, but found an assignment. Did you intend to write '==' ?");
    }
  }
}
