package org.spockframework.builder;

import groovy.lang.MissingPropertyException;
import org.spockframework.gentyref.GenericTypeReflector;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class ListModelTarget implements IModelTarget {
  private final List<Object> list;
  private final Type elementType;
  private final ITypeCoercer coercer;

  public ListModelTarget(List list, Type listType, ITypeCoercer coercer) {
    this.list = list;
    this.elementType = GenericTypeReflector.getTypeParameter(listType, Collection.class.getTypeParameters()[0]);
    this.coercer = coercer;
  }

  public Object getSubject() {
    return list;
  }

  public IModelTarget readSlot(String name) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    if (index == null) throw new MissingPropertyException(name, list.getClass());
    
    Object element = list.get(index);
    return (IModelTarget) coercer.coerce(new ConfigurationValue(element, elementType), IModelTarget.class);
  }

  public void writeSlot(String name, Object value) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    if (index == null) throw new MissingPropertyException(name, list.getClass());
    
    Object element = coercer.coerce(value, elementType);
    list.add(index, element);
  }

  public void configureSlot(String name, List<Object> values, IModelSource source) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    Object element = coercer.coerce(values, elementType);
    if (source != null) {
      IModelTarget target = (IModelTarget) coercer.coerce(
          new ConfigurationValue(element, elementType), IModelTarget.class);
      source.configure(target);
    }
    if (index != null) {
      list.add(index, element);
    } else {
      list.add(element);
    }
  }
}
