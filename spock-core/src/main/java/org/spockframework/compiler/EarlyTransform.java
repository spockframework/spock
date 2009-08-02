/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import spock.lang.*;

/**
 * A transformation for everything that needs to be done before the semantic
 * analysis phase. We try to keep this to a minimum.
 *
 * @author Peter Niederwieser
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class EarlyTransform implements ASTTransformation {
  private final ClassNode PREDEF = ClassHelper.makeWithoutCaching(Predef.class);

  public void visit(ASTNode nodes[], SourceUnit sourceUnit) {
    ModuleNode module = (ModuleNode)nodes[0];
    if (moduleContainsClassWithSpeckAnnotation(module))
      // if this import is already present, it is simply overwritten
      module.addStaticStarImport(Predef.class.getName(), PREDEF);
  }

  private boolean moduleContainsClassWithSpeckAnnotation(ModuleNode module) {
    @SuppressWarnings("unchecked")
    List<ClassNode> classes = module.getClasses();

    for (ClassNode clazz : classes)
      if (hasSpeckAnnotation(clazz))
        return true;

    return false;
  }

  private static boolean hasSpeckAnnotation(ClassNode clazz) {
    @SuppressWarnings("unchecked")
    List<AnnotationNode> annotations = clazz.getAnnotations();

    for (AnnotationNode ann : annotations) {
      String name = ann.getClassNode().getName();
      if (nameResolvesToType(name, Speck.class, clazz.getModule()))
        return true;
    }
    
    return false;
  }

  private static boolean nameResolvesToType(String name, Class<?> type, ModuleNode module) {
    if (name.equals(type.getName())) return true;
    
    String typePackage = type.getPackage().getName() + "."; // oddly, package names in ModuleNode end in "."
    if (name.equals(type.getSimpleName())
        && (typePackage.equals(module.getPackageName()) // same package
           || hasStarImport(module, typePackage)))
      return true;

    // single class import (w/ or wo/ aliasing)
    ImportNode anImport = module.getImport(name);
    return anImport != null && type.getName().equals(anImport.getType().getName());
  }

    private static boolean hasStarImport(ModuleNode module, String typePackage) {
      for (ImportNode anImport : module.getStarImports())
        if (anImport.getPackageName().equals(typePackage))
          return true;

      return false;
    }
}
