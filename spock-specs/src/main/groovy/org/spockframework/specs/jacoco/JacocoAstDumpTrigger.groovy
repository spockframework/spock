package org.spockframework.specs.jacoco

import java.lang.management.ManagementFactory

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.*

/**
 * This is a workaround until https://github.com/gradle/gradle/issues/16438 gets fixed.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.OUTPUT)
class JacocoAstDumpTrigger implements ASTTransformation {
  private static boolean enabled = Boolean.getBoolean(JacocoAstDumpTrigger.class.name)
  private boolean dumped = false

  @Override
  void visit(ASTNode[] nodes, SourceUnit source) {
    if(enabled && !dumped) {
      dumped = true
      new GroovyMBean(ManagementFactory.platformMBeanServer, "org.jacoco:type=Runtime")
        .invokeMethod('dump', true)
    }
  }
}
