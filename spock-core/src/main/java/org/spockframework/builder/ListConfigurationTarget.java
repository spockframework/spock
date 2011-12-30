package org.spockframework.builder;

import groovy.lang.MissingPropertyException;
import org.spockframework.gentyref.GenericTypeReflector;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class ListConfigurationTarget implements IConfigurationTarget {
  private final List<Object> list;
  private final Type elementType;
  private final ITypeCoercer coercer;

  public ListConfigurationTarget(List list, Type listType, ITypeCoercer coercer) {
    this.list = list;
    this.elementType = GenericTypeReflector.getTypeParameter(listType, Collection.class.getTypeParameters()[0]);
    this.coercer = coercer;
  }

  public Object getSubject() {
    return list;
  }

  public IConfigurationTarget readSlot(String name) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    if (index == null) throw new MissingPropertyException(name, list.getClass());
    
    Object element = list.get(index);
    return (IConfigurationTarget) coercer.coerce(new ConfigurationValue(element, elementType), IConfigurationTarget.class);
  }

  public void writeSlot(String name, Object value) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    if (index == null) throw new MissingPropertyException(name, list.getClass());
    
    Object element = coercer.coerce(value, elementType);
    list.add(index, element);
  }

  public void configureSlot(String name, List<Object> values, IConfigurationSource source) {
    Integer index = (Integer) coercer.coerce(name, Integer.class);
    Object element = coercer.coerce(values, elementType);
    if (source != null) {
      IConfigurationTarget target = (IConfigurationTarget) coercer.coerce(
          new ConfigurationValue(element, elementType), IConfigurationTarget.class);
      source.configure(target);
    }
    if (index != null) {
      list.add(index, element);
    } else {
      list.add(element);
    }
  }
}
