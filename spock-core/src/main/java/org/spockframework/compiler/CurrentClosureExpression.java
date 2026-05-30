/*
 * Copyright 2026 the original author or authors.
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
 *
 */

package org.spockframework.compiler;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.classgen.BytecodeExpression;

import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Opcodes;

/**
 * A direct, delegate-independent reference to the closure that is currently being executed.
 *
 * <p>For implicit-{@code this} <em>method</em> conditions inside {@code with} / {@code verifyAll} /
 * {@code verifyEach} closures, {@link org.spockframework.runtime.SpockRuntime#verifyMethodCondition}
 * must be handed the closure itself as the {@code target}, so that method resolution routes through
 * the closure's delegate (with fallback to the owner/spec).
 *
 * <p>Groovy has no source-level keyword for "the closure I am currently in": inside a closure body a
 * value-context {@code this} compiles to {@code getThisObject()} (the enclosing spec), not the closure.
 * Earlier Spock versions worked around this by emitting {@code this.find()} (and before that
 * {@code this.each(Closure.IDENTITY)}), relying on a no-arg DGM resolving against the closure object.
 * Groovy 6 (GROOVY-11858) resolves in-closure implicit-{@code this} calls against the delegate first,
 * which broke that trick.
 *
 * <p>A closure's {@code doCall} method is always an instance method of the generated {@code Closure}
 * subclass, so local variable 0 is the closure instance. Emitting {@code ALOAD 0} therefore yields the
 * current closure directly, without depending on the meta-object protocol at all.
 */
class CurrentClosureExpression extends BytecodeExpression {

  CurrentClosureExpression() {
    super(ClassHelper.CLOSURE_TYPE);
  }

  @Override
  public void visit(MethodVisitor mv) {
    // load "this" of the enclosing doCall method, i.e. the closure instance itself
    mv.visitVarInsn(Opcodes.ALOAD, 0);
  }
}
