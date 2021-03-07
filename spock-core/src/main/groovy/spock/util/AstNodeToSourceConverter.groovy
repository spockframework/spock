package spock.util

import org.spockframework.compat.groovy2.GroovyCodeVisitorCompat

import java.lang.reflect.Modifier
import java.security.CodeSource

import groovy.transform.CompileStatic
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.syntax.Types

/*
 * This has been adapted from https://github.com/apache/groovy/blob/5d2944523f198d96b6515e85a24d2b4e43ce665f/subprojects/groovy-console/src/main/groovy/groovy/console/ui/AstNodeToScriptAdapter.groovy
 * and made backwards compatible with groovy 2.4 via GroovyCodeVisitorCompat.
 *
 * - Replaced getter access by property access
 * - Removed trailing whitespaces when directly followed by printLineBreak()
 * - Special handling of AnnotationConstantExpression in visitConstantExpression
 */

/**
 * This class takes Groovy source code, compiles it to a specific compile phase, and then decompiles it
 * back to the groovy source. It is used by GroovyConsole's AST Browser, but can also be invoked from
 * the command line.
 */
@CompileStatic
class AstNodeToSourceConverter {

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
   * @returns the source code from the AST state
   */

  String compileToScript(String script, int compilePhase, Set<Show> showSet, ClassLoader classLoader = null, CompilerConfiguration config = null) {

    def writer = new StringBuilderWriter()

    classLoader = classLoader ?: new GroovyClassLoader(getClass().classLoader)

    def scriptName = 'script' + System.currentTimeMillis() + '.groovy'
    GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptName, '/groovy/script')
    CompilationUnit cu = new CompilationUnit((CompilerConfiguration)(config ?: CompilerConfiguration.DEFAULT), (CodeSource)codeSource.codeSource, (GroovyClassLoader)classLoader)
    cu.addPhaseOperation(new AstNodeToScriptVisitor(writer, showSet), compilePhase)
    cu.addSource(codeSource.name, script)
    try {
      cu.compile(compilePhase)
    } catch (CompilationFailedException cfe) {

      writer.println 'Unable to produce AST for this phase due to earlier compilation error:'
      cfe.message.eachLine {
        writer.println it
      }
      writer.println 'Fix the above error(s) and then press Refresh'
    } catch (Throwable t) {
      writer.println 'Unable to produce AST for this phase due to an error:'
      writer.println t.message
      writer.println 'Fix the above error(s) and then press Refresh'
    }
    return writer.toString()
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

/**
 * An adapter from ASTNode tree to source code.
 */
@CompileStatic
class AstNodeToScriptVisitor extends CompilationUnit.PrimaryClassNodeOperation implements GroovyClassVisitor, GroovyCodeVisitor, GroovyCodeVisitorCompat {

  public static final EnumSet<Show> CLASS_AND_MEMBERS = EnumSet.of(Show.CLASS, Show.METHODS, Show.FIELDS)
  private final StringBuilderWriter _out
  Stack<String> classNameStack = new Stack<String>()
  String _indent = ''
  boolean readyToIndent = true
  boolean scriptHasBeenVisited
  Set<Show> show

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
    // slightly more complicated than the original code, but avoids creating strings just to check for space at the end
    def builder = _out.builder
    if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ' as Character) {
      if (output.startsWith(' ')) {
        output = output[1..-1]
      }
    }
    _out.print output
  }

  def println(parameter) {
    throw new UnsupportedOperationException('Wrong API')
  }


  private boolean showAny(EnumSet<Show> set) {
    !Collections.disjoint(show, set)
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
    if (!_out.toString().endsWith('\n')) {
      _out.print '\n'
    }
    readyToIndent = true
  }

  def printDoubleBreak() {
    if (_out.toString().endsWith('\n\n')) {
      // do nothing
    } else if (_out.toString().endsWith('\n')) {
      _out.print '\n'
    } else {
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

    if (generics) {
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
    // do not print initial expression, as this is executed as part of the constructor, unless on static constant
    Expression exp = node.initialValueExpression
    if (exp instanceof ConstantExpression) exp = Verifier.transformToPrimitiveConstantIfPossible(exp)
    ClassNode type = exp?.type
    if (Modifier.isStatic(node.modifiers) && Modifier.isFinal(node.modifiers)
      && exp instanceof ConstantExpression
      && type == node.type
      && ClassHelper.isStaticConstantInitializerType(type)) {
      // GROOVY-5150: final constants may be initialized directly
      print ' = '
      if (ClassHelper.STRING_TYPE == type) {
        print "'" + node.initialValueExpression.text.replace("'", "\\'") + "'"
      } else if (ClassHelper.char_TYPE == type) {
        print "'${node.initialValueExpression.text}'"
      } else {
        print node.initialValueExpression.text
      }
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

    if (statement?.collectionExpression instanceof ListExpression) {
      statement?.collectionExpression?.visit this
    } else {
      statement?.collectionExpression?.visit this
    }
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
    if (expression?.arguments instanceof VariableExpression || expression?.arguments instanceof MethodCallExpression) {
      print '('
      expression?.arguments?.visit this
      print ')'
    } else {
      expression?.arguments?.visit this
    }
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
      print ' ->'
    }
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
    expression?.to?.visit this
    print ')'
  }

  @Override
  void visitPropertyExpression(PropertyExpression expression) {
    expression?.objectExpression?.visit this
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

  void visitConstantExpression(ConstantExpression expression, boolean unwrapQuotes = false) {
    if (expression instanceof AnnotationConstantExpression) {
      return // does not convey any useful data in 2.5 and leads to inconsistencies with 3.0
    }
    if (expression.value instanceof String && !unwrapQuotes) {
      // string reverse escaping is very naive
      def escaped = ((String)expression.value).replaceAll('\n', '\\\\n').replaceAll("'", "\\\\'")
      print "'$escaped'"
    } else {
      print expression.value
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

      if (expression?.operation?.text == '[') {
        print ']'
      }
    } else {
      visitType expression?.leftExpression?.type
      visitBinaryExpression expression // is a BinaryExpression
    }
  }

  @Override
  void visitGStringExpression(GStringExpression expression) {
    print '"' + expression.text + '"'
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
    print '(('
    expression?.expression?.visit this
    print ') as '
    visitType(expression?.type)
    print ')'

  }

  /**
   * Prints out the type, safely handling arrays.
   * @param classNode
   */
  void visitType(ClassNode classNode) {
    def name = classNode.name
    if (name =~ /^\[+L/ && name.endsWith(';')) {
      int numDimensions = name.indexOf('L')
      print "${classNode.name[(numDimensions + 1)..-2]}" + ('[]' * numDimensions)
    } else {
      print name
    }
    visitGenerics classNode?.genericsTypes
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
      print '*'            // is this correct?
    } else {
      expression?.keyExpression?.visit this
    }
    print ': '
    expression?.valueExpression?.visit this
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
      ((ASTNode)it).visit this
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
