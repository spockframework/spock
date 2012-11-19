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

package org.spockframework.runtime.extension.builtin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.annotation.Annotation;

import org.junit.*;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.UnreachableCodeError;

/**
 * Adapts the JUnit {@link Before}, {@link After}, {@link BeforeClass} and {@link AfterClass} fixture mechanism to Spock.
 * <p>
 * The method signature requirements that JUnit has for fixture methods apply exactly to Spock as well. That is, fixture methods
 * must return {@code void}, must take no arguments and must be {@code public}. Moreover, {@link Before} and {@link After} fixture
 * methods must be <em>instance</em> methods while {@link BeforeClass} and {@link AfterClass} fixture methods must be <em>static</em>.
 * Any methods that do not meet the requirements will be silently ignored.
 * <p>
 * {@link BeforeClass} fixture methods will be executed <em>once before</em> any feature methods or {@link Before} or {@code setup()} 
 * methods, and <strong>before</strong> the {@code setupSpec()} method (if present). Inheritance semantics are identical 
 * to {@code setupSpec()}, i.e. methods are executed for each class in the hierarchy in turn from <em>parent to child</em>.
 * <p>
 * {@link Before} fixture methods will be executed <em>before every</em> feature method and <strong>before</strong> 
 * the {@code setup()} method (if present). Inheritance semantics are identical to {@code setup()}, i.e. methods are 
 * executed for each class in the hierarchy in turn from <em>parent to child</em>.
 * <p>
 * {@link After} fixture methods will be executed <em>after every</em> feature method and <strong>after</strong> 
 * the {@code cleanup()} method (if present). Inheritance semantics are identical to {@code cleanup()}, i.e. methods are 
 * executed for each class in the hierarchy in turn from <em>child to parent</em>.
 * <p>
 * {@link AfterClass} fixture methods will be executed <em>once after</em> all feature methods and after {@link After} and 
 * {@code cleanup()} methods, and <strong>after</strong> the {@code cleanupSpec()} method (if present). Inheritance semantics 
 * are identical to {@code cleanupSpec()}, i.e. methods are executed for each class in the hierarchy in turn from <em>child to parent</em>.
 * <p>   
 * The execution order of fixture methods of the same type withing the same class is undefined (as it is with JUnit).
 * 
 * @author Luke Daley
 */
@SuppressWarnings("UnusedDeclaration")
public class JUnitFixtureMethodsExtension implements IGlobalExtension {
  
  private static enum FixtureType {
    BEFORE(Before.class, false, MethodKind.SETUP, true),
    AFTER(After.class, false, MethodKind.CLEANUP, false),
    BEFORE_CLASS(BeforeClass.class, true, MethodKind.SETUP_SPEC, true),
    AFTER_CLASS(AfterClass.class, true, MethodKind.CLEANUP_SPEC, false);
    
    public final Class<? extends Annotation> annotationType;
    public final boolean isStatic;
    public final MethodKind interceptedMethodKind;
    public final boolean executeBeforeSpecMethod;
    
    FixtureType(Class<? extends Annotation> annotationType, boolean isStatic, MethodKind interceptedMethodKind, boolean executeBeforeSpecMethod) {
      this.annotationType = annotationType;
      this.isStatic = isStatic;
      this.interceptedMethodKind = interceptedMethodKind;
      this.executeBeforeSpecMethod = executeBeforeSpecMethod;
    }
    
    private void addInterceptor(SpecInfo specInfo, Collection<Method> potentialMethods) {
      List<Method> fixtureMethods = new LinkedList<Method>();
      for (Method method : potentialMethods) {
        if (isMethod(method)) fixtureMethods.add(method);
      }
      
      if (!fixtureMethods.isEmpty()) {
        getInterceptedMethod(specInfo).addInterceptor(new FixtureMethodInterceptor(fixtureMethods));
      }
    }
    
    private MethodInfo getInterceptedMethod(SpecInfo specInfo) {
      for (MethodInfo methodInfo : specInfo.getFixtureMethods()) {
        if (methodInfo.getKind().equals(interceptedMethodKind)) return methodInfo;
      }
      
      throw new UnreachableCodeError("failed to find fixture method of kind " + interceptedMethodKind);
    }
    
    // NOTE - method is assumed to have passed isPotentialMethod
    private boolean isMethod(Method method) {
      boolean isMethod = method.getAnnotation(annotationType) != null
          && Modifier.isStatic(method.getModifiers()) == isStatic;
          
      return isMethod;
    }
        
    static public boolean isPotentialMethod(Method method) {
      boolean isPotential =  method.getReturnType().equals(void.class) 
          && method.getParameterTypes().length == 0 
          && Modifier.isPublic(method.getModifiers());
          
      return isPotential;
    }
    
    static public void addInterceptors(SpecInfo spec) {
      Map<FixtureType, Set<String>> appliedMethods=new HashMap<JUnitFixtureMethodsExtension.FixtureType, Set<String>>();
      
      for (SpecInfo currentSpec : spec.getSpecsBottomToTop()) {
        List<Method> potentialMethods = new LinkedList<Method>();
        for (Method method : currentSpec.getReflection().getDeclaredMethods()) {
          if (isPotentialMethod(method)) {
            potentialMethods.add(method);
          }
        }

        if (!potentialMethods.isEmpty()) {
          for (FixtureType fixtureType : FixtureType.values()) {
            Set<String> appliedMethodsForFixtureType = appliedMethods.get(fixtureType);
            if(appliedMethodsForFixtureType==null) {
              appliedMethodsForFixtureType=new HashSet<String>();
              appliedMethods.put(fixtureType, appliedMethodsForFixtureType);
            }
            List<Method> filteredMethods = new LinkedList<Method>();
            for (Method method : potentialMethods) {
              if(!appliedMethodsForFixtureType.contains(method.getName())) {
                filteredMethods.add(method);
                appliedMethodsForFixtureType.add(method.getName());                
              }
            }
            if (!filteredMethods.isEmpty()) {
              fixtureType.addInterceptor(currentSpec, filteredMethods);
            }
          }
        }
      }
    }
    
    private class FixtureMethodInterceptor implements IMethodInterceptor {
      private final Collection<Method> methods;

      public FixtureMethodInterceptor(Collection<Method> methods) {
        this.methods = methods;
      }

      public void intercept(IMethodInvocation invocation) throws Throwable {
        if (!executeBeforeSpecMethod) invocation.proceed();

        for (Method method : methods) {
          ReflectionUtil.invokeMethod(invocation.getTarget(), method);
        }

        if (executeBeforeSpecMethod) invocation.proceed();
      }
    }
  }
  
  public void visitSpec(SpecInfo spec) {
    FixtureType.addInterceptors(spec);
  }
  
}