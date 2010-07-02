/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * A Groovy builder for JSON values.
 *
 * <pre>
 * def books1 = builder.books {
 * book = [title: "The Definitive Guide to Grails", author: "Graeme Rocher"]
 * book = [title: "The Definitive Guide to Grails", author: "Graeme Rocher"]
 * }
 *
 * def books2 = builder.books {
 * book = new Book(title: "The Definitive Guide to Grails",
 * author: "Graeme Rocher")
 * book = new Book(title: "The Definitive Guide to Grails",
 * author: "Graeme Rocher")
 * }
 *
 * def books3 = builder.books {
 * book = {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * book = {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * }
 *
 * def books4 = builder.books {
 * book {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * book {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * }
 *
 * def books5 = builder.books {
 * 2.times {
 * book = {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * }
 * }
 *
 * def books6 = builder.books {
 * 2.times {
 * book {
 * title = "The Definitive Guide to Grails"
 * author= "Graeme Rocher"
 * }
 * }
 * }
 *
 *
 * all 6 books variables output the same JSON
 *
 * {"books": {
 * "book": [{
 * "title": "The Definitive Guide to Grails",
 * "author": "Graeme Rocher"
 * },{
 * "title": "The Definitive Guide to Grails",
 * "author": "Graeme Rocher"
 * }]
 * }
 * }
 * </pre>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JsonGroovyBuilder extends GroovyObjectSupport {
  private static final String JSON = "json";
  private JSON current;
  private Map properties;
  private Stack stack;

  public JsonGroovyBuilder() {
    stack = new Stack();
    properties = new HashMap();
  }

  public Object getProperty(String name) {
    if (!stack.isEmpty()) {
      Object top = stack.peek();
      if (top instanceof JSONObject) {
        JSONObject json = (JSONObject) top;
        if (json.containsKey(name)) {
          return json.get(name);
        } else {
          return _getProperty(name);
        }
      } else {
        return _getProperty(name);
      }
    } else {
      return _getProperty(name);
    }
  }

  public Object invokeMethod(String name, Object arg) {
    if (JSON.equals(name) && stack.isEmpty()) {
      return createObject(name, arg);
    }

    Object[] args = (Object[]) arg;
    if (args.length == 0) {
      throw new MissingMethodException(name, getClass(), args);
    }

    Object value = null;
    if (args.length > 1) {
      JSONArray array = new JSONArray();
      stack.push(array);
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof Closure) {
          append(name, createObject((Closure) args[i]));
        } else if (args[i] instanceof Map) {
          append(name, createObject((Map) args[i]));
        } else if (args[i] instanceof List) {
          append(name, createArray((List) args[i]));
        } else {
          _append(name, args[i], (JSON) stack.peek());
        }
      }
      stack.pop();
    } else {
      if (args[0] instanceof Closure) {
        value = createObject((Closure) args[0]);
      } else if (args[0] instanceof Map) {
        value = createObject((Map) args[0]);
      } else if (args[0] instanceof List) {
        value = createArray((List) args[0]);
      }
    }

    if (stack.isEmpty()) {
      JSONObject object = new JSONObject();
      object.accumulate(name, current);
      current = object;
    } else {
      JSON top = (JSON) stack.peek();
      if (top instanceof JSONObject) {
        append(name, current == null ? value : current);
      }
    }

    return current;
  }

  public void setProperty(String name, Object value) {
    if (value instanceof GString) {
      value = value.toString();
      try {
        value = JSONSerializer.toJSON(value);
      } catch (JSONException jsone) {
        // its a String literal
      }
    } else if (value instanceof Closure) {
      value = createObject((Closure) value);
    } else if (value instanceof Map) {
      value = createObject((Map) value);
    } else if (value instanceof List) {
      value = createArray((List) value);
    }

    append(name, value);
  }

  private Object _getProperty(String name) {
    if (properties.containsKey(name)) {
      return properties.get(name);
    } else {
      return super.getProperty(name);
    }
  }

  private void append(String key, Object value) {
    Object target = null;
    if (!stack.isEmpty()) {
      target = stack.peek();
      current = (JSON) target;
      _append(key, value, current);
    } else {
      properties.put(key, value);
    }
  }

  private void _append(String key, Object value, JSON target) {
    if (target instanceof JSONObject) {
      ((JSONObject) target).accumulate(key, value);
    } else if (target instanceof JSONArray) {
      ((JSONArray) target).element(value);
    }
  }

  private JSON createArray(List list) {
    JSONArray array = new JSONArray();
    stack.push(array);
    for (Iterator elements = list.iterator(); elements.hasNext();) {
      Object element = elements.next();
      if (element instanceof Closure) {
        element = createObject((Closure) element);
      } else if (element instanceof Map) {
        element = createObject((Map) element);
      } else if (element instanceof List) {
        element = createArray((List) element);
      }
      array.element(element);
    }
    stack.pop();
    return array;
  }

  private JSON createObject(Closure closure) {
    JSONObject object = new JSONObject();
    stack.push(object);
    closure.setDelegate(this);
    closure.call();
    stack.pop();
    return object;
  }

  private JSON createObject(Map map) {
    JSONObject object = new JSONObject();
    stack.push(object);
    for (Iterator properties = map.entrySet()
        .iterator(); properties.hasNext();) {
      Map.Entry property = (Map.Entry) properties.next();
      String key = String.valueOf(property.getKey());
      Object value = property.getValue();
      if (value instanceof Closure) {
        value = createObject((Closure) value);
      } else if (value instanceof Map) {
        value = createObject((Map) value);
      } else if (value instanceof List) {
        value = createArray((List) value);
      }
      object.element(key, value);
    }
    stack.pop();
    return object;
  }

  private JSON createObject(String name, Object arg) {
    Object[] args = (Object[]) arg;
    if (args.length == 0) {
      throw new MissingMethodException(name, getClass(), args);
    }

    if (args.length == 1) {
      if (args[0] instanceof Closure) {
        return createObject((Closure) args[0]);
      } else if (args[0] instanceof Map) {
        return createObject((Map) args[0]);
      } else if (args[0] instanceof List) {
        return createArray((List) args[0]);
      } else {
        throw new JSONException("Unsupported type");
      }
    } else {
      JSONArray array = new JSONArray();
      stack.push(array);
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof Closure) {
          append(name, createObject((Closure) args[i]));
        } else if (args[i] instanceof Map) {
          append(name, createObject((Map) args[i]));
        } else if (args[i] instanceof List) {
          append(name, createArray((List) args[i]));
        } else {
          _append(name, args[i], (JSON) stack.peek());
        }
      }
      stack.pop();
      return array;
    }
  }
}