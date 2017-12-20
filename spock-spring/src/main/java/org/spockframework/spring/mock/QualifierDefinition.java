
/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.mock;

import org.spockframework.spring.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Definition of a Spring {@link Qualifier @Qualifier}.
 *
 * original authors Phillip Webb, Stephane Nicoll
 * @see Definition
 */
class QualifierDefinition {

  private final Field field;

  private final DependencyDescriptor descriptor;

  private final Set<Annotation> annotations;

  QualifierDefinition(Field field, Set<Annotation> annotations) {
    // We can't use the field or descriptor as part of the context key
    // but we can assume that if two fields have the same qualifiers then
    // it's safe for Spring to use either for qualifier logic
    this.field = field;
    this.descriptor = new DependencyDescriptor(field, true);
    this.annotations = annotations;
  }

  public boolean matches(ConfigurableListableBeanFactory beanFactory, String beanName) {
    return beanFactory.isAutowireCandidate(beanName, this.descriptor);
  }

  public void applyTo(RootBeanDefinition definition) {
    definition.setQualifiedElement(this.field);
  }

  @Override
  public int hashCode() {
    return this.annotations.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
      return false;
    }
    QualifierDefinition other = (QualifierDefinition)obj;
    return this.annotations.equals(other.annotations);
  }

  public static QualifierDefinition forElement(AnnotatedElement element) {
    if (element != null && element instanceof Field) {
      Field field = (Field)element;
      Set<Annotation> annotations = getQualifierAnnotations(field);
      if (!annotations.isEmpty()) {
        return new QualifierDefinition(field, annotations);
      }
    }
    return null;
  }

  private static Set<Annotation> getQualifierAnnotations(Field field) {
    // Assume that any annotations other than @MockBean/@SpyBean are qualifiers
    Annotation[] candidates = field.getDeclaredAnnotations();
    Set<Annotation> annotations = new HashSet<>(candidates.length);
    for (Annotation candidate : candidates) {
      if (!isMockOrSpyAnnotation(candidate)) {
        annotations.add(candidate);
      }
    }
    return annotations;
  }

  private static boolean isMockOrSpyAnnotation(Annotation candidate) {
    Class<? extends Annotation> type = candidate.annotationType();
    return (type.equals(SpringBean.class) || type.equals(SpringSpy.class));
  }

}
