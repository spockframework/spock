package org.spockframework.util;

import org.spockframework.runtime.GroovyRuntimeUtil;

import java.util.*;

import groovy.lang.Range;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.w3c.dom.Element;

public abstract class RenderUtil {
  /**
   * Returns true if the Object is handled properly by {@link org.codehaus.groovy.runtime.InvokerHelper#inspect(java.lang.Object)}
   * or {@link org.codehaus.groovy.runtime.InvokerHelper#toString(Object)}, either by special handling or if the Class has
   * an own {@code toString()} implementation.
   */
  public static boolean isHandledByInspectOrToString(@Nullable Object arguments) {
    if (arguments == null) {
      return true;
    }
    if (arguments instanceof Range) {
      return true;
    }
    if (arguments instanceof Collection) {
      return true;
    }
    if (arguments instanceof Map) {
      return true;
    }
    if (arguments instanceof Element) {
      return true;
    }
    if (arguments instanceof String) {
      return true;
    }

    Class<?> argumentsClass = arguments.getClass();
    if (argumentsClass.isArray()) {
      return argumentsClass.getComponentType().isPrimitive()
        || ReflectionUtil.isToStringOverridden(argumentsClass.getComponentType());
    }

    return ReflectionUtil.isToStringOverridden(argumentsClass);
  }

  public static String toStringOrDump(@Nullable Object value) {
    if (isHandledByInspectOrToString(value)) {
      return GroovyRuntimeUtil.toString(value);
    }
    if (value instanceof Wrapper) {
      Wrapper wrapper = (Wrapper) value;
      return String.format("%s as %s", toStringOrDump(wrapper.unwrap()), wrapper.getType().getSimpleName());
    }
    return dump(value);
  }

  public static String inspectOrDump(@Nullable Object value) {
    if (isHandledByInspectOrToString(value)) {
      return DefaultGroovyMethods.inspect(value);
    }
    if (value instanceof Wrapper) {
      Wrapper wrapper = (Wrapper) value;
      return String.format("%s as %s", inspectOrDump(wrapper.unwrap()), wrapper.getType().getSimpleName());
    }
    return dump(value);
  }

  private static String dump(@Nullable Object value) {
    if (value.getClass().isArray()) {
      return dumpArrayString((Object[])value);
    } else {
      return DefaultGroovyMethods.dump(value);
    }
  }


  /*
   * Adapted from org.codehaus.groovy.runtime.InvokerHelper.toArrayString(java.lang.Object[]) to use dump()
   */
  private static String dumpArrayString(Object[] arguments) {
    StringBuilder argBuf = new StringBuilder("[");
    for (int i = 0; i < arguments.length; i++) {
      if (i > 0) {
        argBuf.append(", ");
      }
      argBuf.append(DefaultGroovyMethods.dump(arguments[i]));
    }
    argBuf.append("]");
    return argBuf.toString();
  }

}
