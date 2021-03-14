package org.spockframework.junit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.MethodKind;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class JUnit4AnnotationLifecycleMethodsExtension implements IGlobalExtension {

  @Override
  public void visitSpec(SpecInfo spec) {
    Class<?> clazz = spec.getReflection();
    new JUnit4LifecycleSpecInfoEnhancer(spec, clazz, clazz).enhance();
  }

  private static class JUnit4LifecycleSpecInfoEnhancer {
    private final SpecInfo spec;
    private final Class<?> clazz;
    private final Class<?> effectiveClass;

    private JUnit4LifecycleSpecInfoEnhancer(SpecInfo spec, Class<?> clazz, Class<?> effectiveClass) {
      this.spec = spec;
      this.clazz = clazz;
      this.effectiveClass = effectiveClass;
    }

    void enhance() {
      handleSuperSpec();

      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Before.class)) {
          spec.addSetupMethod(createJUnitFixtureMethod(method, MethodKind.SETUP, Before.class));
        }
        if (method.isAnnotationPresent(After.class)) {
          spec.addCleanupMethod(createJUnitFixtureMethod(method, MethodKind.CLEANUP, After.class));
        }
        if (method.isAnnotationPresent(BeforeClass.class)) {
          spec.addSetupSpecMethod(createJUnitFixtureMethod(method, MethodKind.SETUP_SPEC, BeforeClass.class));
        }
        if (method.isAnnotationPresent(AfterClass.class)) {
          spec.addCleanupSpecMethod(createJUnitFixtureMethod(method, MethodKind.CLEANUP_SPEC, AfterClass.class));
        }
      }
    }

    private void handleSuperSpec() {
      Class<?> superClass = spec.getReflection().getSuperclass();
      if (!(superClass == Object.class || superClass == Specification.class)) {
        new JUnit4LifecycleSpecInfoEnhancer(spec.getSuperSpec(), superClass, clazz).enhance();
      }
    }

    private MethodInfo createJUnitFixtureMethod(Method method, MethodKind kind, Class<? extends Annotation> annotation) {
      MethodInfo methodInfo = createMethod(method, kind, method.getName());
      methodInfo.setExcluded(isOverriddenJUnitFixtureMethod(method, annotation));
      return methodInfo;
    }

    private MethodInfo createMethod(Method method, MethodKind kind, String name) {
      MethodInfo methodInfo = new MethodInfo();
      methodInfo.setParent(spec);
      methodInfo.setName(name);
      methodInfo.setReflection(method);
      methodInfo.setKind(kind);
      return methodInfo;
    }

    private boolean isOverriddenJUnitFixtureMethod(Method method, Class<? extends Annotation> annotation) {
      if (Modifier.isPrivate(method.getModifiers())) return false;

      for (Class<?> currClass = effectiveClass; currClass != clazz; currClass = currClass.getSuperclass()) {
        for (Method currMethod : currClass.getDeclaredMethods()) {
          if (!currMethod.isAnnotationPresent(annotation)) continue;
          if (!currMethod.getName().equals(method.getName())) continue;
          if (!Arrays.deepEquals(currMethod.getParameterTypes(), method.getParameterTypes())) continue;
          return true;
        }
      }

      return false;
    }
  }

}
