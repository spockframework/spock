/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.builder.json;

import java.util.*;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;

import net.sf.json.*;

import org.spockframework.builder.*;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.Nullable;

import groovy.lang.*;

public class JsonGestalt implements IGestalt {
  private static final String ARRAY_ELEMENT = "_";

  private JSON subject;
  private final IBlueprint blueprint;
  private final Object context;

  public JsonGestalt(@Nullable JSON subject, IBlueprint blueprint, Object context) {
    this.subject = subject;
    this.blueprint = blueprint;
    this.context = context;
  }

  public JSON getSubject() {
    return subject;
  }

  public IBlueprint getBlueprint() {
    return blueprint;
  }

  public Object getProperty(String name) {
    try {
      return InvokerHelper.getProperty(context, name);
    } catch (MissingPropertyException e) {
      if (subject instanceof JSONObject && ((JSONObject) subject).has(name))
        return ((JSONObject) subject).get(name);
      throw e;
    }
  }

  public void setProperty(String name, Object value) {
    InvokerHelper.setProperty(context, name, value);
    // TODO: redirect to (do)invokeMethod()
  }

  // TODO: need to provide a way to set JSON null value
  public Object invokeMethod(String name, Object[] args) {
    try {
      return InvokerHelper.invokeMethod(context, name, args);
    } catch (MissingMethodException e) {}
    
    if (args.length == 0)
      throw new IllegalArgumentException(String.format("Missing value for element '%s'", name));

    Closure closure = null;
    Object lastArg = args[args.length - 1];
    if (lastArg instanceof Closure) {
      closure = (Closure) lastArg;
      Object[] newArgs = new Object[args.length - 1];
      System.arraycopy(args, 0, newArgs, 0, newArgs.length);
      args = newArgs;
    }

    Object value;

    switch (args.length) {
      case 0:
        value = null;
        break;
      case 1:
        value = args[0];
        break;
      default:
        value = args;
    }

    if (value != null && value.getClass().isArray()) {
      JSONArray array = new JSONArray();
      array.addAll(Arrays.asList((Object[]) value));
      value = array;
    } else if (value instanceof Collection) {
      if (!(value instanceof JSONArray)) {
        JSONArray array = new JSONArray();
        array.addAll((Collection) value);
        value = array;
      }
    } else if (value instanceof Map) {
      if (!(value instanceof JSONObject)) {
        JSONObject obj = new JSONObject();
        obj.accumulateAll((Map) value);
      }
    }

    if (closure != null) {
      if (!(value == null || value instanceof JSONArray || value instanceof JSONObject))
        throw new IllegalArgumentException(String.format("Cannot add elements to a value that's neither a JSON array nor a JSON object: %s", value));

      JsonGestalt childGestalt = new JsonGestalt((JSON) value, new ClosureBlueprint(closure, value), context);
      new Sculpturer().$form(childGestalt);
      value = childGestalt.subject;
    }

    if (subject == null)
      subject = name.equals(ARRAY_ELEMENT) ? new JSONArray() : new JSONObject();

    if (name.equals(ARRAY_ELEMENT)) {
      if (!(subject instanceof JSONArray))
        throw new IllegalStateException("Cannot add unnamed element to JSON object");
      ((JSONArray) subject).add(value);
    } else {
      if (!(subject instanceof JSONObject))
        throw new IllegalStateException("Cannot add named element to JSON array");
      ((JSONObject) subject).accumulate(name, value);
    }

    return value;
  }


}
