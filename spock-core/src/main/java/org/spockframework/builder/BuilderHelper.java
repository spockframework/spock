/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.builder;

import java.lang.reflect.Modifier;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.spockframework.runtime.GroovyRuntimeUtil;

public class BuilderHelper {
  public static Object createInstance(Class clazz, Object... args) {
    if (args.length == 1) {
      Object arg = args[0];
      if (MetaClassHelper.isAssignableFrom(clazz, arg.getClass())) return arg;
      // IDEA: could do additional coercions here, like Groovy does when setting a property
      // (note that we don't know if we are setting a property or invoking a method):
      // int -> byte (at least if it fits), etc.
    }

    // IDEA: could support creation of collection types here

    if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
      String kind = clazz.isPrimitive() ? "primitive" : clazz.isInterface() ? "interface" : "abstract";
      throw new RuntimeException(String.format( "Cannot instantiate %s type %s", kind, clazz.getName()));
    }

    // TODO: need exception handling for better error messages?
    return GroovyRuntimeUtil.invokeConstructor(clazz, args);
  }
}
