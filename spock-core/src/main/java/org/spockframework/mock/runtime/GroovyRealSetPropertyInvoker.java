/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.util.ThreadSafe;

/**
 * A {@link GroovyRealMethodInvoker} which calls the real {@link MetaClass#setProperty(Class, Object, String, Object, boolean, boolean)} as a fallback, if property was not found via the setter method.
 *
 * @author Andreas Turban
 * @since 2.4
 */
@ThreadSafe
public class GroovyRealSetPropertyInvoker extends GroovyRealMethodInvoker {
  private static final Class<?>[] SETTER_MISSING_ARGS = {String.class, Object.class};

  private final MetaClass metaClass;
  private final Class<?> sender;
  private final String property;
  private final boolean useSuper;
  private final boolean fromInsideClass;

  public GroovyRealSetPropertyInvoker(MetaClass metaClass, Class<?> sender, String property, boolean useSuper, boolean fromInsideClass) {
    super(metaClass);
    this.metaClass = metaClass;
    this.sender = sender;
    this.property = property;
    this.useSuper = useSuper;
    this.fromInsideClass = fromInsideClass;
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    try {
      return super.respond(invocation);
    } catch (InvokerInvocationException | MissingMethodException e1) {
      Object receiver = invocation.getMockObject().getInstance();
      Object newValue = invocation.getArguments().get(0);
      try {
        metaClass.setProperty(sender, receiver, property, newValue, useSuper, fromInsideClass);
        return null;
      } catch (InvokerInvocationException | MissingMethodException e2) {
        try {
          return handleMissingProperty(receiver, property, newValue);
        } catch (MissingPropertyException e3) {
          e3.addSuppressed(e2);
          e3.addSuppressed(e1);
          throw e3;
        }
      }
    }
  }

  private Object handleMissingProperty(Object target, String property, Object newValue) {
    //https://issues.apache.org/jira/browse/GROOVY-11781
    //Since Groovy 5: Groovy uses getProperty() and setProperty() for field access of outer classes.
    //So we need to implement the "property missing" workflow from MetaClassImpl.getProperty().
    if (isTargetStatic(target)) {
      return invokeStaticMissingProperty(target, property, newValue);
    }

    return metaClass.invokeMissingProperty(target, property, newValue, false);
  }
  
  private Object invokeStaticMissingProperty(Object target, String property, Object newValue) {
    MetaMethod propertyMissing = metaClass.getMetaMethod(GroovyRealGetPropertyInvoker.STATIC_PROPERTY_MISSING, SETTER_MISSING_ARGS);
    if (propertyMissing != null) {
      return propertyMissing.invoke(target, new Object[]{property, newValue});
    }

    throw new MissingPropertyException(property, (Class<?>) target);
  }

  private boolean isTargetStatic(Object target) {
    return target instanceof Class && metaClass.getTheClass() != Class.class;
  }
}
