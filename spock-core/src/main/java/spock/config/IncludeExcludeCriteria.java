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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration indicating which specs and methods should be
 * included/excluded from a spec run. Specs can be included/excluded
 * based on their annotations and their (base) classes. Methods
 * can be included/excluded based on their annotations and names.
 * Criteria can be annotations, spec classes, base classes,
 * and regex pattern strings.  Patterns are matched against the
 * simple names of annotations, spec (but not base) classes,
 * and methods.
 *
 * @author Peter Niederwieser
 */
public class IncludeExcludeCriteria {
  @SuppressWarnings("unchecked")
  public IncludeExcludeCriteria(Object... criteria) {
    for (Object criterium : criteria)
      if (criterium instanceof Class) {
        Class<?> classCriterium = (Class<?>) criterium;
        if (classCriterium.isAnnotation())
          annotations.add((Class<? extends Annotation>)classCriterium);
        else
          baseClasses.add(classCriterium);
      } else if (criterium instanceof String) {
        patterns.add(Pattern.compile((String) criterium));
      } else {
        throw new ConfigurationException("unsupported criterium, must be pattern string or class: %s", criterium.toString());
      }
  }
  
  public List<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>();
  public List<Class<?>> baseClasses = new ArrayList<Class<?>>();
  public List<Pattern> patterns = new ArrayList<Pattern>();

  public boolean isEmpty() {
    return annotations.isEmpty() && baseClasses.isEmpty() && patterns.isEmpty();
  }
}
