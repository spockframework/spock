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

package spock.config;

import java.util.ArrayList;
import java.util.List;

abstract class IncludeExclude {
  public IncludeExclude(Object... args) {
    for (Object arg : args)
      if (arg instanceof IncludeExcludeAnnotation)
        annotations.add((IncludeExcludeAnnotation)arg);
      else if (arg instanceof Class)
        isAs.add((Class)arg);
      else throw new ConfigurationException("expected IncludeExcludeAnnotation or Class, but got %s", arg.getClass());
  }
  
  public List<IncludeExcludeAnnotation> annotations = new ArrayList<IncludeExcludeAnnotation>();
  public List<Class<?>> isAs = new ArrayList<Class<?>>();
}
