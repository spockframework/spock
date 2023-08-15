package spock.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.TraceClassVisitor
import org.spockframework.compat.groovy2.GroovyCodeVisitorCompat

import java.lang.reflect.Modifier
import java.security.CodeSource

import groovy.transform.*
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.syntax.Types

import static java.util.Collections.disjoint

/*
 * This has been adapted from https://github.com/apache/groovy/blob/5d2944523f198d96b6515e85a24d2b4e43ce665f/subprojects/groovy-console/src/main/groovy/groovy/console/ui/AstNodeToScriptAdapter.groovy
 * and made backwards compatible with groovy 2.5 via GroovyCodeVisitorCompat.
 *
 * - Replaced getter access by property access
 * - Removed trailing whitespaces when directly followed by printLineBreak()
 * - Special handling of AnnotationConstantExpression in visitConstantExpression
 * - Fix AnnotationNode rendering
 * - Improve GString rendering
 */

/**
 * This class takes Groovy source code, compiles it to a specific compile phase, and then decompiles it
 * back to the groovy source.
 */
@CompileStatic
class SourceToAstNodeAndSourceTranspiler {

  /**
   * This method takes source code, compiles it, then reverses it back to source.
   *
   * @param script
   *    the source code to be compiled. If invalid, a compile error occurs
   * @param compilePhase
   *    the CompilePhase. Must be an int mapped in {@link CompilePhase}
   * @param showSet
   *    the set of code to include in the rendering
   * @param classLoader
   *    (optional) the classloader to use. If missing/null then the current is used.
   *    This parameter enables things like ASTBrowser to invoke this with the correct classpath
   * @param config
   *    optional compiler configuration
   * @returns the source code from the AST state and the captured ast nodes
   */
  TranspileResult compileScript(String script, int compilePhase, Set<Show> showSet, ClassLoader classLoader = null, CompilerConfiguration config = null) {

    def writer = new StringBuilderWriter()

    classLoader = classLoader ?: new GroovyClassLoader(getClass().classLoader)

    def scriptName = 'script.groovy'
    GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptName, '/groovy/script')
    CompilationUnit cu = new CompilationUnit((CompilerConfiguration)(config ?: CompilerConfiguration.DEFAULT), (CodeSource)codeSource.codeSource, (GroovyClassLoader)classLoader)
    def captureVisitor = new AstNodeCaptureVisitor()
    cu.addPhaseOperation(captureVisitor, compilePhase)
    cu.addSource(codeSource.name, script)

    boolean shallTraceBytecode = compilePhase >= CompilePhase.CLASS_GENERATION.phaseNumber
    if (shallTraceBytecode) {
      cu.setClassgenCallback(createClassgenCallback(writer, showSet))
    } else {
      cu.addPhaseOperation(new AstNodeToScriptVisitor(writer, showSet), compilePhase)
    }

    try {
      cu.compile(compilePhase)
    } catch (CompilationFailedException cfe) {

      writer.println 'Unable to produce AST for this phase due to earlier compilation error:'
      cfe.message.eachLine {
        writer.println it
      }
    }

    return new TranspileResult(writer.toString(), captureVisitor.nodeCaptures)
  }

  @CompileDynamic
  private static CompilationUnit.ClassgenCallback createClassgenCallback(Writer writer, Set<Show> showSet) {
    return { classVisitor, classNode ->
      //We need here @CompileDynamic that we can access the toByteArray() method from classVisitor
      byte[] data = classVisitor.toByteArray()
      disassembleByteCodeForClass(writer, data, showSet)
    }
  }

  private static void disassembleByteCodeForClass(Writer writer, byte[] bytes, Set<Show> showSet) {
    def traceVisitor = new FilteringClassVisitor(new TraceClassVisitor(new PrintWriter(writer)), showSet)
    new ClassReader(new ByteArrayInputStream(bytes)).accept(traceVisitor, 0)
  }
}

@CompileStatic
@PackageScope
class FilteringClassVisitor extends ClassVisitor {
  private static final int JAVA8_CLASS_VERSION = 52

  private final Set<Show> showSet

  FilteringClassVisitor(ClassVisitor cv, Set<Show> showSet) {
    super(Opcodes.ASM9, cv)
    this.showSet = showSet
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    //Override class file version to Java 8 as a default, because otherwise different Groovy/Java combinations will create different outputs.
    super.visit(JAVA8_CLASS_VERSION, access, name, signature, superName, interfaces)
  }

  @Override
  org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (showSet.contains(Show.ANNOTATIONS)) {
      return super.visitAnnotation(descriptor, visible)
    }
    return null
  }

  @Override
  MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    if (name == "<init>") {
      if (showSet.contains(Show.CONSTRUCTORS)) {
        return new FilteringMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), showSet)
      }
    } else {
      if (showSet.contains(Show.METHODS) &&
          (access & Opcodes.ACC_SYNTHETIC) == 0 &&
          name != "setMetaClass" &&
          name != "getMetaClass") {
        return new FilteringMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), showSet)
      }
    }
    return null
  }

  @Override
  FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    if (showSet.contains(Show.FIELDS)) {
      return super.visitField(access, name, descriptor, signature, value)
    }
    return null
  }
}

@CompileStatic
@PackageScope
class FilteringMethodVisitor extends MethodVisitor {
  private final Set<Show> showSet

  FilteringMethodVisitor(MethodVisitor cv, Set<Show> showSet) {
    super(Opcodes.ASM9, cv)
    this.showSet = showSet
  }

  @Override
  org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (showSet.contains(Show.ANNOTATIONS)) {
      return super.visitAnnotation(descriptor, visible)
    }
    return null
  }
}

@CompileStatic
enum Show {
  /**
   * Show the package declaration portion of the source code
   */
  PACKAGE,
  /**
   * Show the imports declaration portion of the source code
   */
  IMPORTS,
  /**
   * Show the script portion of the source code
   */
  SCRIPT,
  /**
   * Show the Script class from the source code
   */
  CLASS,
  /**
   * Show the declared constructors of a class
   *
   * Implicit constructors are not rendered
   */
  CONSTRUCTORS,
  /**
   * Show the object initializers of a class
   */
  OBJECT_INITIALIZERS,
  /**
   * Show the methods of a class
   */
  METHODS,
  /**
   * Show the fields of a class
   */
  FIELDS,
  /**
   * Show the properties of a class
   */
  PROPERTIES,
  /**
   * Show the Annotations
   *
   * Is present, annotations will be rendered for the other selected elements.
   * If nothing else is selected, nothing will be rendered.
   */
  ANNOTATIONS;

  static EnumSet<Show> all() {
    EnumSet.allOf(Show)
  }
}

@TupleConstructor
@CompileStatic
class TranspileResult {
  final String source
  final List<NodeCapture> nodeCaptures
}


@TupleConstructor
@CompileStatic
class NodeCapture {
  final SourceUnit source
  final ClassNode classNode
}

@CompileStatic
class AstNodeCaptureVisitor extends CompilationUnit.PrimaryClassNodeOperation {

  final List<NodeCapture> nodeCaptures = []

  @Override
  void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
    nodeCaptures << new NodeCapture(source, classNode)
  }
}


/**
 * An adapter from ASTNode tree to source code.
 */
@CompileStatic
class AstNodeToScriptVisitor extends CompilationUnit.PrimaryClassNodeOperation implements GroovyClassVisitor, GroovyCodeVisitor, GroovyCodeVisitorCompat {

  public static final EnumSet<Show> CLASS_AND_MEMBERS = EnumSet.of(Show.CLASS, Show.METHODS, Show.FIELDS)
  private static final char SPACE = ' '
  private static final char NEW_LINE = '\n'
  private final StringBuilderWriter _out
  private final Set<Show> show
  private Stack<String> classNameStack = new Stack<String>()
  private String _indent = ''
  private boolean readyToIndent = true
  private boolean scriptHasBeenVisited

  AstNodeToScriptVisitor(StringBuilderWriter writer, Set<Show> show) {
    this._out = writer
    this.show = show
    this.scriptHasBeenVisited = false
  }

  void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {

    showOn(Show.PACKAGE) {
      visitPackage(source?.AST?.package)
    }

    showOn(Show.PACKAGE) {
      visitAllImports(source)
    }

    if (show.contains(Show.SCRIPT) && !scriptHasBeenVisited) {
      scriptHasBeenVisited = true
      source?.AST?.statementBlock?.visit(this)
    }
    if (showAny(CLASS_AND_MEMBERS) || !classNode.script) {
      visitClass classNode
    }
  }

  private def visitAllImports(SourceUnit source) {
    boolean staticImportsPresent = false
    boolean importsPresent = false

    source?.AST?.staticImports?.values()?.each {
      visitImport(it)
      staticImportsPresent = true
    }
    source?.AST?.staticStarImports?.values()?.each {
      visitImport(it)
      staticImportsPresent = true
    }

    if (staticImportsPresent) {
      printDoubleBreak()
    }

    source?.AST?.imports?.each {
      visitImport(it)
      importsPresent = true
    }
    source?.AST?.starImports?.each {
      visitImport(it)
      importsPresent = true
    }
    if (importsPresent) {
      printDoubleBreak()
    }
  }


  void print(parameter) {
    String output = parameter.toString()

    if (readyToIndent) {
      _out.print _indent
      readyToIndent = false
      while (output.startsWith(' ')) {
        output = output[1..-1]  // trim left
      }
    }
    if (outEndsWith(SPACE)) {
      if (output.startsWith(' ')) {
        output = output[1..-1]
      }
    }
    _out.print output
  }

  // slightly more complicated than the original code, but avoids creating strings just to check for the end
  boolean outEndsWith(Character character) {
    def builder = _out.builder
    return (builder.length() > 0 && builder.charAt(builder.length() - 1) == character)
  }

  boolean outEndsWith(String  str) {
    def builder = _out.builder
    return (builder.length() > str.size() && builder.substring(builder.length() - str.size()) == str)
  }

  void trimSpaceRight() {
    while (outEndsWith(SPACE)) {
      _out.builder.length = _out.builder.length() - 1
    }
  }

  def println(parameter) {
    throw new UnsupportedOperationException('Wrong API')
  }

  private boolean showAny(EnumSet<Show> set) {
    !disjoint(show, set)
  }

  def showOn(Show s, Closure block) {
    if (show.contains(s)) {
      block()
    }
  }

  def showOnAll(Set<Show> s, Closure block) {
    if (show.containsAll(s)) {
      block()
    }
  }

  def indented(Closure block) {
    String startingIndent = _indent
    _indent = _indent + '    '
    block()
    _indent = startingIndent
  }

  def printLineBreak() {
    if (!outEndsWith(NEW_LINE)) {
      trimSpaceRight()
      _out.print '\n'
    }
    readyToIndent = true
  }

  def printDoubleBreak() {
    if (outEndsWith('\n\n')) {
      // do nothing
    } else if (outEndsWith(NEW_LINE)) {
      _out.print '\n'
    } else {
      trimSpaceRight()
      _out.print '\n'
      _out.print '\n'
    }
    readyToIndent = true
  }

  void visitPackage(PackageNode packageNode) {

    if (packageNode) {

      showOn(Show.ANNOTATIONS) {
        packageNode.annotations?.each {
          visitAnnotationNode(it)
          printLineBreak()
        }
      }

      if (packageNode.text.endsWith('.')) {
        print packageNode.text[0..-2]
      } else {
        print packageNode.text
      }
      printDoubleBreak()
    }
  }

  void visitImport(ImportNode node) {
    if (node) {
      showOn(Show.ANNOTATIONS) {
        node.annotations?.each {
          visitAnnotationNode(it)
          printLineBreak()
        }
      }
      print node.text
      printLineBreak()
    }
  }

  @Override
  void visitClass(ClassNode node) {

    classNameStack.push(node.name)

    showOnAll(EnumSet.of(Show.ANNOTATIONS, Show.CLASS)) {
      node?.annotations?.each {
        visitAnnotationNode(it)
        printLineBreak()
      }
    }


    showOn(Show.CLASS) {
      visitModifiers(node.modifiers)
      if (node.interface) print node.name
      else print "class $node.name"
      visitGenerics node?.genericsTypes
      print ' extends '
      visitType node.unresolvedSuperClass
      boolean first = true
      node.unresolvedInterfaces?.each {
        if (first) {
          print ' implements '
        } else {
          print ', '
        }
        first = false
        visitType it
      }
      print ' {'
      printDoubleBreak()
    }

    readyToIndent = true

    indented {
      showOn(Show.PROPERTIES) {
        node?.properties?.each { visitProperty(it) }
        printLineBreak()
      }
      showOn(Show.FIELDS) {
        node?.fields?.each { visitField(it) }
        printDoubleBreak()
      }
      showOn(Show.CONSTRUCTORS) {
        node?.declaredConstructors?.each { visitConstructor(it) }
        printLineBreak()
      }
      showOn(Show.OBJECT_INITIALIZERS) {
        visitObjectInitializerBlocks(node)
        printLineBreak()
      }
      showOn(Show.METHODS) {
        node?.methods?.each { visitMethod(it) }
      }
    }
    showOn(Show.CLASS) {
      print '}'
      printLineBreak()
      classNameStack.pop()
    }
  }

  private void visitObjectInitializerBlocks(ClassNode node) {
    for (Statement stmt : (node.objectInitializerStatements)) {
      print '{'
      printLineBreak()
      indented {
        stmt.visit(this)
      }
      printLineBreak()
      print '}'
      printDoubleBreak()
    }
  }

  private void visitGenerics(GenericsType[] generics) {

    if (generics != null) {
      print '<'
      boolean first = true
      generics.each { GenericsType it ->
        if (!first) {
          print ', '
        }
        first = false
        print it.name
        if (it.upperBounds) {
          print ' extends '
          boolean innerFirst = true
          it.upperBounds.each { ClassNode upperBound ->
            if (!innerFirst) {
              print ' & '
            }
            innerFirst = false
            visitType upperBound
          }
        }
        if (it.lowerBound) {
          print ' super '
          visitType it.lowerBound
        }
      }
      print '>'
    }
  }

  @Override
  void visitConstructor(ConstructorNode node) {
    visitMethod(node)
  }

  private String visitParameters(parameters) {
    boolean first = true

    parameters.each { Parameter p ->
      if (!first) {
        print ', '
      }
      first = false

      showOn(Show.ANNOTATIONS) {
        p.annotations?.each {
          visitAnnotationNode(it)
          print(' ')
        }
      }

      visitModifiers(p.modifiers)
      visitType p.type
      print ' ' + p.name
      if (p.initialExpression && !(p.initialExpression instanceof EmptyExpression)) {
        print ' = '
        p.initialExpression.visit this
      }
    }
  }

  @Override
  void visitMethod(MethodNode node) {
    showOn(Show.ANNOTATIONS) {
      node?.annotations?.each {
        visitAnnotationNode(it)
        printLineBreak()
      }
    }

    visitModifiers(node.modifiers)
    if (node.name == '<init>') {
      print "${classNameStack.peek()}("
      visitParameters(node.parameters)
      print ') {'
      printLineBreak()
    } else if (node.name == '<clinit>') {
      print '{ ' // will already have 'static' from modifiers
      printLineBreak()
    } else {
      if (node.genericsTypes) {
        visitGenerics node.genericsTypes
        print ' '
      }
      visitType node.returnType
      print " $node.name("
      visitParameters(node.parameters)
      print ')'
      if (node.exceptions) {
        boolean first = true
        print ' throws '
        node.exceptions.each {
          if (!first) {
            print ', '
          }
          first = false
          visitType it
        }
      }
      print ' {'
      printLineBreak()
    }

    indented {
      node?.code?.visit(this)
    }
    printLineBreak()
    print '}'
    printDoubleBreak()
  }

  private void visitModifiers(int modifiers) {
    String mods = Modifier.toString(modifiers)
    mods = mods ? mods + ' ' : mods
    print mods
  }

  @Override
  void visitField(FieldNode node) {

    showOn(Show.ANNOTATIONS) {
      node?.annotations?.each {
        visitAnnotationNode(it)
        printLineBreak()
      }
    }
    visitModifiers(node.modifiers)
    visitType node.type
    print " $node.name "
    // Groovy's version didn't render initialValueExpression with this explanation:
    // > do not print initial expression, as this is executed as part of the constructor, unless on static constant
    // but since we want to see what is going on, we do render it
    Expression exp = node.initialValueExpression
    if (exp) {
      print ' = '
      exp.visit(this)
    }
    printLineBreak()
  }

  void visitAnnotationNode(AnnotationNode node) {
    print '@' + node?.classNode?.name
    if (node?.members) {
      print '('
      boolean first = true
      node.members.each { String name, Expression value ->
        if (first) {
          first = false
        } else {
          print ', '
        }
        print name + ' = '
        value.visit(this)
      }
      print ')'
    }

  }

  @Override
  void visitProperty(PropertyNode node) {
    // is a FieldNode, avoid double dispatch
  }

  @Override
  void visitBlockStatement(BlockStatement block) {
    if (printStatementLabels(block)) {
      print '{'
      printLineBreak()
      indented {
        block?.statements?.each {
          it.visit(this)
          printLineBreak()
        }
      }
      print '}'
      printLineBreak()
    } else {
      block?.statements?.each {
        it.visit(this)
        printLineBreak()
      }
    }
    if (!_out.toString().endsWith('\n')) {
      printLineBreak()
    }
  }

  @Override
  void visitForLoop(ForStatement statement) {
    printStatementLabels(statement)
    print 'for ('
    if (statement?.variable != ForStatement.FOR_LOOP_DUMMY) {
      visitParameters([statement.variable])
      print ' : '
    }

    statement?.collectionExpression?.visit this
    print ') {'
    printLineBreak()
    indented {
      statement?.loopBlock?.visit this
    }
    print '}'
    printLineBreak()
  }

  @Override
  void visitIfElse(IfStatement ifElse) {
    printStatementLabels(ifElse)
    print 'if ('
    ifElse?.booleanExpression?.visit this
    print ') {'
    printLineBreak()
    indented {
      ifElse?.ifBlock?.visit this
    }
    printLineBreak()
    if (ifElse?.elseBlock && !(ifElse.elseBlock instanceof EmptyStatement)) {
      print '} else {'
      printLineBreak()
      indented {
        ifElse?.elseBlock?.visit this
      }
      printLineBreak()
    }
    print '}'
    printLineBreak()
  }

  @Override
  void visitExpressionStatement(ExpressionStatement statement) {
    statement.expression.visit this
  }

  @Override
  void visitReturnStatement(ReturnStatement statement) {
    printLineBreak()
    print 'return '
    statement.expression.visit(this)
    printLineBreak()
  }

  @Override
  void visitSwitch(SwitchStatement statement) {
    printStatementLabels(statement)
    print 'switch ('
    statement?.expression?.visit this
    print ') {'
    printLineBreak()
    indented {
      statement?.caseStatements?.each {
        visitCaseStatement it
      }
      if (statement?.defaultStatement) {
        print 'default: '
        printLineBreak()
        statement?.defaultStatement?.visit this
      }
    }
    print '}'
    printLineBreak()
  }

  @Override
  void visitCaseStatement(CaseStatement statement) {
    print 'case '
    statement?.expression?.visit this
    print ':'
    printLineBreak()
    indented {
      statement?.code?.visit this
    }
  }

  @Override
  void visitBreakStatement(BreakStatement statement) {
    print 'break'
    if (statement?.label) {
      print ' ' + statement.label
    }
    printLineBreak()
  }

  @Override
  void visitContinueStatement(ContinueStatement statement) {
    print 'continue'
    if (statement?.label) {
      print ' ' + statement.label
    }
    printLineBreak()
  }

  @Override
  void visitMethodCallExpression(MethodCallExpression expression) {

    Expression objectExp = expression.objectExpression
    if (objectExp instanceof VariableExpression) {
      visitVariableExpression(objectExp, false)
    } else {
      objectExp.visit(this)
    }
    if (expression.spreadSafe) {
      print '*'
    }
    if (expression.safe) {
      print '?'
    }
    print '.'
    Expression method = expression.method
    if (method instanceof ConstantExpression) {
      visitConstantExpression(method, true)
    } else {
      method.visit(this)
    }
    expression.arguments.visit(this)
  }

  @Override
  void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
    print expression?.ownerType?.name + '.' + expression?.method
    expression?.arguments?.visit this
  }

  @Override
  void visitConstructorCallExpression(ConstructorCallExpression expression) {
    if (expression?.superCall) {
      print 'super'
    } else if (expression?.thisCall) {
      print 'this '
    } else {
      print 'new '
      visitType expression?.type
    }
    expression?.arguments?.visit this
  }

  @Override
  void visitBinaryExpression(BinaryExpression expression) {
    expression?.leftExpression?.visit this
    if (!(expression.rightExpression instanceof EmptyExpression) || expression.operation.type != Types.ASSIGN) {
      print " $expression.operation.text "
      expression.rightExpression.visit this

      if (expression?.operation?.text == '[') {
        print ']'
      }
    }
  }

  @Override
  void visitPostfixExpression(PostfixExpression expression) {
    print '('
    expression?.expression?.visit this
    print ')'
    print expression?.operation?.text
  }

  @Override
  void visitPrefixExpression(PrefixExpression expression) {
    print expression?.operation?.text
    print '('
    expression?.expression?.visit this
    print ')'
  }


  @Override
  void visitClosureExpression(ClosureExpression expression) {
    print '{ '
    if (expression?.parameters) {
      visitParameters(expression?.parameters)
    }
    print ' ->'
    printLineBreak()
    indented {
      expression?.code?.visit this
    }
    print '}'
  }

  @Override
  void visitLambdaExpression(LambdaExpression expression) {
    print '( '
    if (expression?.parameters) {
      visitParameters(expression?.parameters)
    }
    print ') -> {'
    printLineBreak()
    indented {
      expression?.code?.visit this
    }
    print '}'
  }

  @Override
  void visitTupleExpression(TupleExpression expression) {
    print '('
    visitExpressionsAndCommaSeparate(expression?.expressions)
    print ')'
  }

  @Override
  void visitRangeExpression(RangeExpression expression) {
    print '('
    expression?.from?.visit this
    print '..'
    if (!expression?.inclusive) {
      print '<'
    }
    expression?.to?.visit this
    print ')'
  }

  @Override
  void visitPropertyExpression(PropertyExpression expression) {
    expression?.objectExpression?.visit this
    trimSpaceRight() // remove space inserted by previous expression
    if (expression?.spreadSafe) {
      print '*'
    } else if (expression?.safe) {
      print '?'
    }
    print '.'
    if (expression?.property instanceof ConstantExpression) {
      visitConstantExpression((ConstantExpression)expression?.property, true)
    } else {
      expression?.property?.visit this
    }
  }

  @Override
  void visitAttributeExpression(AttributeExpression attributeExpression) {
    visitPropertyExpression attributeExpression
  }

  @Override
  void visitFieldExpression(FieldExpression expression) {
    print expression?.field?.name
  }

  private static String escapeCharacters(String input) {
    input
      ?.replaceAll('\\\\', '\\\\\\\\')
      ?.replaceAll('\b', '\\\\b')
      ?.replaceAll('\f', '\\\\f')
      ?.replaceAll('\n', '\\\\n')
      ?.replaceAll('\r', '\\\\r')
      ?.replaceAll('\t', '\\\\t')
      ?.replaceAll("'", "\\\\'")
      ?.replaceAll('"', '\\\\"')
      ?.replaceAll('\\$', '\\\\\\$')
  }

  void visitConstantExpression(ConstantExpression expression, boolean unwrapQuotes = false, Boolean escapeChars = null) {
    if (expression instanceof AnnotationConstantExpression) {
      visitAnnotationNode (expression.value as AnnotationNode)
      return
    }
    if ((expression.value instanceof String || expression.value instanceof Character) && !unwrapQuotes) {
      //noinspection GroovyPointlessBoolean
      String value = escapeChars == false ? expression.value : escapeCharacters(expression.value as String)
      print "'$value'"
    } else {
      //noinspection GroovyPointlessBoolean
      print escapeChars == true ? escapeCharacters(expression.value as String) : expression.value
    }
  }

  @Override
  void visitClassExpression(ClassExpression expression) {
    print expression.text
  }

  void visitVariableExpression(VariableExpression expression, boolean spacePad = true) {

    if (spacePad) {
      print ' ' + expression.name + ' '
    } else {
      print expression.name
    }
  }

  @Override
  void visitDeclarationExpression(DeclarationExpression expression) {
    // handle multiple assignment expressions
    if (expression?.leftExpression instanceof ArgumentListExpression) {
      print 'def '
      visitArgumentlistExpression((ArgumentListExpression)expression?.leftExpression, true)
      print " $expression.operation.text "
      expression.rightExpression.visit this
    } else {
      visitType expression?.leftExpression?.type
      visitBinaryExpression expression // is a BinaryExpression
    }
  }

  @Override
  void visitGStringExpression(GStringExpression expression) {
    print '"'
    ((expression.strings as Collection<Expression>) + expression.values)
      .sort { left, right ->
        left.lineNumber <=> right.lineNumber
          ?: left.columnNumber <=> right.columnNumber
      }
      .each {
        if (it in expression.values) {
          if (it instanceof ClosureExpression) {
            print '$'
            it.visit(this)
          } else {
            print '${'
            if (it instanceof VariableExpression) {
              visitVariableExpression(it, false)
            } else {
              it.visit(this)
            }
            print '}'
          }
        } else if (it instanceof ConstantExpression) {
          visitConstantExpression(it, true, true)
        }
      }
    print '"'
  }

  @Override
  void visitSpreadExpression(SpreadExpression expression) {
    print '*'
    expression?.expression?.visit this
  }

  @Override
  void visitNotExpression(NotExpression expression) {
    print '!('
    expression?.expression?.visit this
    print ')'
  }

  @Override
  void visitUnaryMinusExpression(UnaryMinusExpression expression) {
    print '-('
    expression?.expression?.visit this
    print ')'
  }

  @Override
  void visitUnaryPlusExpression(UnaryPlusExpression expression) {
    print '+('
    expression?.expression?.visit this
    print ')'
  }

  @Override
  void visitCastExpression(CastExpression expression) {
    if (expression.coerce) {
      print '(('
      expression?.expression?.visit this
      print ') as '
      visitType(expression?.type)
      print ')'
    } else {
      print '(('
      visitType(expression?.type)
      print ') '
      expression?.expression?.visit this
      print ')'
    }
  }

  /**
   * Prints out the type, safely handling arrays.
   * @param classNode
   */
  void visitType(ClassNode classNode) {
    if (classNode.array || classNode.genericsPlaceHolder) {
      print classNode.toString(false)
    } else {
      print classNode.name
      visitGenerics classNode?.genericsTypes
    }
  }

  void visitArgumentlistExpression(ArgumentListExpression expression, boolean showTypes = false) {
    print '('
    int count = expression?.expressions?.size()
    expression.expressions.each {
      if (showTypes) {
        visitType it.type
        print ' '
      }
      if (it instanceof VariableExpression) {
        visitVariableExpression it, false
      } else if (it instanceof ConstantExpression) {
        visitConstantExpression it, false
      } else {
        it.visit this
      }
      count--
      if (count) print ', '
    }
    print ')'
  }

  @Override
  void visitBytecodeExpression(BytecodeExpression expression) {
    print '/*BytecodeExpression*/'
    printLineBreak()
  }


  @Override
  void visitMapExpression(MapExpression expression) {
    print '['
    if (expression?.mapEntryExpressions?.size() == 0) {
      print ':'
    } else {
      visitExpressionsAndCommaSeparate((List)expression?.mapEntryExpressions)
    }
    print ']'
  }

  @Override
  void visitMapEntryExpression(MapEntryExpression expression) {
    if (expression?.keyExpression instanceof SpreadMapExpression) {
      expression?.keyExpression?.visit this
    } else {
      expression?.keyExpression?.visit this
      print ': '
      expression?.valueExpression?.visit this
    }
  }

  @Override
  void visitListExpression(ListExpression expression) {
    print '['
    visitExpressionsAndCommaSeparate(expression?.expressions)
    print ']'
  }

  @Override
  void visitTryCatchFinally(TryCatchStatement statement) {
    printStatementLabels(statement)
    print 'try {'
    printLineBreak()
    indented {
      statement?.tryStatement?.visit this
    }
    printLineBreak()
    print '}'
    printLineBreak()
    statement?.catchStatements?.each { CatchStatement catchStatement ->
      visitCatchStatement(catchStatement)
    }
    print 'finally {'
    printLineBreak()
    indented {
      statement?.finallyStatement?.visit this
    }
    print '}'
    printLineBreak()
  }

  @Override
  void visitThrowStatement(ThrowStatement statement) {
    print 'throw '
    statement?.expression?.visit this
    printLineBreak()
  }

  @Override
  void visitSynchronizedStatement(SynchronizedStatement statement) {
    printStatementLabels(statement)
    print 'synchronized ('
    statement?.expression?.visit this
    print ') {'
    printLineBreak()
    indented {
      statement?.code?.visit this
    }
    print '}'
  }

  @Override
  void visitTernaryExpression(TernaryExpression expression) {
    expression?.booleanExpression?.visit this
    print ' ? '
    expression?.trueExpression?.visit this
    print ' : '
    expression?.falseExpression?.visit this
  }

  @Override
  void visitShortTernaryExpression(ElvisOperatorExpression expression) {
    visitTernaryExpression(expression)
  }

  @Override
  void visitBooleanExpression(BooleanExpression expression) {
    expression?.expression?.visit this
  }

  @Override
  void visitWhileLoop(WhileStatement statement) {
    printStatementLabels(statement)
    print 'while ('
    statement?.booleanExpression?.visit this
    print ') {'
    printLineBreak()
    indented {
      statement?.loopBlock?.visit this
    }
    printLineBreak()
    print '}'
    printLineBreak()
  }

  @Override
  void visitDoWhileLoop(DoWhileStatement statement) {
    printStatementLabels(statement)
    print 'do {'
    printLineBreak()
    indented {
      statement?.loopBlock?.visit this
    }
    print '} while ('
    statement?.booleanExpression?.visit this
    print ')'
    printLineBreak()
  }

  @Override
  void visitCatchStatement(CatchStatement statement) {
    print 'catch ('
    visitParameters([statement.variable])
    print ') {'
    printLineBreak()
    indented {
      statement.code?.visit this
    }
    print '}'
    printLineBreak()
  }

  @Override
  void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
    print '~('
    expression?.expression?.visit this
    print ') '
  }

  @Override
  void visitAssertStatement(AssertStatement statement) {
    print 'assert '
    statement?.booleanExpression?.visit this
    print ' : '
    statement?.messageExpression?.visit this
  }

  @Override
  void visitClosureListExpression(ClosureListExpression expression) {
    boolean first = true
    expression?.expressions?.each {
      if (!first) {
        print ';'
      }
      first = false
      it.visit this
    }
  }

  @Override
  void visitMethodPointerExpression(MethodPointerExpression expression) {
    expression?.expression?.visit this
    trimSpaceRight() // remove space inserted by previous expression
    print '.&'
    expression?.methodName?.visit this
  }

  @Override
  void visitMethodReferenceExpression(MethodReferenceExpression expression) {
    expression?.expression?.visit this
    print '::'
    expression?.methodName?.visit this
  }

  @Override
  void visitArrayExpression(ArrayExpression expression) {
    print 'new '
    visitType expression?.elementType
    print '['
    visitExpressionsAndCommaSeparate(expression?.sizeExpression)
    print ']'
    if (expression?.expressions) { // print array initializer
      print '{'
      visitExpressionsAndCommaSeparate(expression?.expressions)
      print '}'
    }
  }

  private void visitExpressionsAndCommaSeparate(List<? super Expression> expressions) {
    boolean first = true
    expressions?.each {
      if (!first) {
        print ', '
      }
      first = false
      /*
      I have no idea why the AnnotationConstantExpression does it,
      but it first visits the values of the members before it visits itself.
      That's why you got org.spockframework.runtime.model.BlockKind.SETUP followed by [] followed by the actual correct representation.
      https://issues.apache.org/jira/browse/GROOVY-9980
       */
      if (it instanceof AnnotationConstantExpression) {
        visitConstantExpression(it)
      } else {
        (it as ASTNode).visit this
      }
    }
  }

  @Override
  void visitSpreadMapExpression(SpreadMapExpression expression) {
    print '*:'
    expression?.expression?.visit this
  }

  /**
   * Prints all labels for the given statement.  The labels will be printed on a single
   * line and line break will be added.
   *
   * @param statement for which to print labels
   * @return {@code true} if the statement had labels to print, else {@code false}
   */
  private boolean printStatementLabels(Statement statement) {
    List<String> labels = statement?.statementLabels
    if (labels == null || labels.empty) {
      return false
    }
    for (String label : labels) {
      print label + ':'
      printLineBreak()
    }
    return true
  }
}
