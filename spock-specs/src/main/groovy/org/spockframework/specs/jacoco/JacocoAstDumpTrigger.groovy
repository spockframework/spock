package org.spockframework.specs.jacoco

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.util.ReleaseInfo

import javax.management.MBeanServerConnection
import java.lang.management.ManagementFactory

/**
 * This is a workaround until https://github.com/gradle/gradle/issues/16438 gets fixed.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.OUTPUT)
class JacocoAstDumpTrigger implements ASTTransformation {
  private static boolean enabled = Boolean.getBoolean(JacocoAstDumpTrigger.class.simpleName)
  private boolean dumped = false

  @Override
  void visit(ASTNode[] nodes, SourceUnit source) {
    if (enabled && !dumped) {
      dumped = true
      String mbeanName = ReleaseInfo.version.startsWith('2')
        ? 'groovy.util.GroovyMBean'
        : 'groovy.jmx.GroovyMBean'

      Class.forName(mbeanName)
        .getConstructor(MBeanServerConnection, String)
        .newInstance(ManagementFactory.platformMBeanServer, "org.jacoco:type=Runtime")
        .invokeMethod('dump', true)
    }
  }
}
