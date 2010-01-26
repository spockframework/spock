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

package spock.builder;

import groovy.lang.Script;

public class ScriptBlueprint implements IBlueprint {
  private final Script script;

  public ScriptBlueprint(Script script) {
    this.script = script;
  }

  public void setDelegate(Object delegate) {
    // the following is from ConfigSlurper
    // might need this to correctly handle the case where a given script instance is
    // used as blueprint more than once
    //GroovySystem.metaClassRegistry.removeMetaClass(script.class)
//    MetaClass metaClass = InvokerHelper.getMetaClass(script);
//    MetaMethod oldInvokeMethod = metaClass.pickMethod("invokeMethod", new Class[] {String.class, Object.class});
//    script.metaClass.invokeMethod = { String name, args ->
//      try {
//        delegate.invokeMethod(name, args)
//      } catch (MissingMethodException e) {
//        oldInvokeMethod.invoke(script, name, args)
//      }
//    }
  }

  public void evaluate() {
    script.run();
  }
}