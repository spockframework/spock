/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.mock;

import org.spockframework.runtime.SpecInfoBuilder;
import org.spockframework.runtime.model.*;
import org.spockframework.spring.*;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Specification;

import java.util.*;

class DefinitionsParser {

  private Set<Definition> definitions = new LinkedHashSet<>();

  DefinitionsParser() {
  }

  void parse(Object object) {
    // we only support spock specs
    if (object instanceof Class) {
      Class<?> clazz = (Class<?>)object;
      if (Specification.class.isAssignableFrom(clazz)) {
        inspect(clazz);
      }
    } else if (object instanceof Specification) {
      inspect(object.getClass());
    }
  }

  private void inspect(Class<?> clazz) {
    // Add stub beans first so that they might be replaced by SpringBeans later
    List<StubBeans> stubBeans = ReflectionUtil.collectAnnotationRecursive(clazz, StubBeans.class);
    for (StubBeans stubBean : stubBeans) {
      for (Class<?> stub : stubBean.value()) {
        definitions.add(new StubDefinition(stub));
      }
      if (!stubBean.inherit()) break;
    }

    SpecInfo specInfo = new SpecInfoBuilder(clazz).build();
    List<FieldInfo> fields = specInfo.getAllFields();
    for (FieldInfo field : fields) {
      if (field.isAnnotationPresent(SpringBean.class)) {
        definitions.add(new SpockDefinition(field));
      } else if (field.isAnnotationPresent(SpringSpy.class)) {
        definitions.add(new SpyDefinition(field));
      }
    }
  }

  Set<Definition> getDefinitions() {
    return definitions;
  }
}
