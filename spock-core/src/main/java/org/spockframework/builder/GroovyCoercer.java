package org.spockframework.builder;

import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.GroovyRuntimeUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class GroovyCoercer implements ITypeCoercer {
  public Object coerce(Object value, Type type) {
    Class<?> clazz = GenericTypeReflector.erase(type);
    try {
      // try to get around weird last fallback in DefaultGroovyMethods#asType(Object, Class)
      // cannot simply do multi-dispatch to asType ourselves and replace asType(Object, Class)
      // with DefaultTypeTransformation#castToType() because other asType() methods delegate
      // to asType(Object, Class) too
      if (clazz.isInterface()) {
        if (!(Number.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
        || Map.class.isAssignableFrom(clazz)))
          return null;
      }
      // need multi-dispatch, hence we cannot call DefaultTypeTransformation.castToType directly
      return GroovyRuntimeUtil.invokeMethod(value, "asType", clazz);
    } catch (GroovyCastException e) {
      return null;
    }
  }
}
