package org.spockframework.compiler.condition;

import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;

public interface IConditionErrorRecorders {

  default void defineValueRecorder(List<Statement> stats) {
    defineValueRecorder(stats, "");
  }

  void defineValueRecorder(List<Statement> stats, String variableNameSuffix);

  default void defineErrorCollector(List<Statement> stats) {
    defineErrorCollector(stats, "");
  }

  void defineErrorCollector(List<Statement> stats, String variableNameSuffix);

  void defineErrorRethrower(List<Statement> stats);

}
