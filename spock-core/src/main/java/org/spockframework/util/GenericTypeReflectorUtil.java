package org.spockframework.util;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * {@link GenericTypeReflectorUtil} provides the API, which Spock uses from the <a href="https://github.com/leangen/geantyref">geantyref</a> library.
 *
 * <p>Please do <b>not</b> use geantyref (except for {@link TypeToken}) directly in the Spock, please use this class instead.
 * This will help with the maintenance of the geantyref library dependency in the future, e.g. we can fix bugs here or make our own adaptions.
 * Also we can write tests for the expectations Spock has to the library.</p>
 */
public abstract class GenericTypeReflectorUtil {

  /**
   * Returns the erasure of the given type.
   */
  public static Class<?> erase(Type type) {
    return GenericTypeReflector.erase(type);
  }

  /**
   * Resolves the parameter types of the given method/constructor in the given type,  with the method {@link GenericTypeReflector#getParameterTypes(Executable, Type)}.
   *
   * <p>In difference to the method of the library, this method will resolve unknown {@link java.lang.reflect.TypeVariable}s to the upper bound of the type variable.
   *
   * @param executable    the executable code for which the parameter types shall be resolved
   * @param declaringType the declaring type of the {@code executable}
   * @return the resolved types of the parameters
   */
  public static Type[] getParameterTypes(Executable executable, Type declaringType) {
    Type[] parameterTypes = GenericTypeReflector.getParameterTypes(executable, declaringType);
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = resolveTypeVariableWithBound(parameterTypes[i]);
    }
    return parameterTypes;
  }

  /**
   * Returns the display name of a Type.
   */
  public static String getTypeName(Type type) {
    return GenericTypeReflector.getTypeName(type);
  }

  /**
   * Returns the exact type of the given field in the given type.
   * This may be different from <tt>f.getGenericType()</tt> when the field was declared in a superclass,
   * of <tt>type</tt> is a raw type.
   *
   * @param f    the field to resolve
   * @param type the type to resolve the field with
   * @return the resolved type
   */
  public static Type getExactFieldType(Field f, Type type) {
    return GenericTypeReflector.getExactFieldType(f, type);
  }

  /**
   * Resolves the return type of the given method in the given type, with the method {@link GenericTypeReflector#getReturnType(Method, Type)}.
   *
   * <p>In difference to the method of the library, this method will resolve unknown {@link java.lang.reflect.TypeVariable}s to the upper bound of the type variable.
   *
   * @param method        the method for which the return type shall be resolved
   * @param declaringType the declaring type of the {@code method}
   * @return the resolved type
   */
  public static Type getReturnType(Method method, Type declaringType) {
    Type returnType = GenericTypeReflector.getReturnType(method, declaringType);
    return resolveTypeVariableWithBound(returnType);
  }

  private static Type resolveTypeVariableWithBound(Type type) {
    if (type instanceof TypeVariable) {
      List<Class<?>> upperBounds = GenericTypeReflector.getUpperBoundClassAndInterfaces(type);
      if (upperBounds.size() == 1) {
        return upperBounds.get(0);
      } else {
        return Object.class;
      }
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      for (Type actualTypeArgument : paramType.getActualTypeArguments()) {
        if (actualTypeArgument instanceof TypeVariable) {
          //We still have TypeVariable in the parameter type, we can't construct a new ParameterizedType easily here, so erase the type
          return GenericTypeReflector.erase(type);
        }
      }
    }
    return type;
  }
}
