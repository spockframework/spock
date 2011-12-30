package org.spockframework.builder;

import java.lang.reflect.Type;
import java.util.*;

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.GroovyRuntimeUtil;

// coercions to primitives, BigInteger, and BigDecimal are already
// handled by GroovyCoercer
// IDEA: could also support maps
public class StringCoercer implements ITypeCoercer {
  private final ITypeCoercer masterCoercer;

  public StringCoercer(ITypeCoercer masterCoercer) {
    this.masterCoercer = masterCoercer;
  }

  // TODO: handle arrays
  public Object coerce(Object value, Type type) {
    if (!(value instanceof String)) return null;

    Class<?> clazz = GenericTypeReflector.erase(type);
    if (!Collection.class.isAssignableFrom(clazz)) return null;
      
    Collection<Object> coll = createCollection(clazz);
    if (coll == null) return null;

    String[] elements = ((String) value).split(",");
    Type elementType = getElementType(type);
    for (String element: elements) {
      coll.add(masterCoercer.coerce(element.trim(), elementType));
    }
    
    return coll;
  }

  // we could potentially do this via GroovyCoercer (coerce an empty list)
  // but GroovyCoercer can't handle some types such as Deque
  @SuppressWarnings("unchecked")
  private Collection<Object> createCollection(Class<?> clazz) {
    if (clazz.isInterface()) {
      return createCollectionFromInterfaceType(clazz);
    }
    return createCollectionFromClassType(clazz);
  }

  private Collection<Object> createCollectionFromInterfaceType(Class<?> clazz) {
    if (clazz == List.class) {
      return new ArrayList<Object>();
    }
    if (clazz == Set.class) {
      return new HashSet<Object>();
    }
    if (clazz == SortedSet.class || clazz == NavigableSet.class) {
      return new TreeSet<Object>(); // does this make sense w/o knowing appropriate comparator?
    }
    if (clazz == Queue.class || clazz == Deque.class) {
      return new LinkedList<Object>();
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> createCollectionFromClassType(Class<?> type) {
    try {
      return (Collection<Object>) GroovyRuntimeUtil.invokeConstructor(type);
    } catch (Exception e) {
      return null;
    }
  }

  private Type getElementType(Type collectionType) {
    return GenericTypeReflector.getTypeParameter(collectionType, Collection.class.getTypeParameters()[0]);
  }
}
