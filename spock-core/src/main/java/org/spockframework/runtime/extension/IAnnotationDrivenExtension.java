/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.extension;

import java.lang.annotation.Annotation;
import java.util.List;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.model.*;

/**
 * @author Peter Niederwieser
 */
public interface IAnnotationDrivenExtension<T extends Annotation> {
  /**
   * Handles the annotation when applied to a specification one or multiple times.
   * The default implementation of this method calls {@link #visitSpecAnnotation(Annotation, SpecInfo)}
   * once for each given annotation.
   *
   * @since 2.0
   * @param annotations the annotations found on the specification
   * @param spec        the annotated specification
   */
  default void visitSpecAnnotations(List<T> annotations, SpecInfo spec) {
    for (T annotation : annotations) {
      visitSpecAnnotation(annotation, spec);
    }
  }

  /**
   * Handles the annotation when applied to a specification.
   * The default implementation of this method throws an {@link InvalidSpecException}.
   * This method is not called by Spock directly, but only by the default implementation of
   * {@link #visitSpecAnnotations(List, SpecInfo)}, so if you have overwritten that method,
   * there is no need to overwrite this one too except if you want to call it yourself.
   *
   * @param annotation the annotation found on the specification
   * @param spec       the annotated specification
   */
  default void visitSpecAnnotation(T annotation, SpecInfo spec) {
    throw new InvalidSpecException("@%s may not be applied to Specs")
      .withArgs(annotation.annotationType().getSimpleName());
  }

  /**
   * Handles the annotation when applied to a field of a specification one or multiple times.
   * The default implementation of this method calls {@link #visitFieldAnnotation(Annotation, FieldInfo)}
   * once for each given annotation.
   *
   * @since 2.0
   * @param annotations the annotations found on the field
   * @param field       the annotated field
   */
  default void visitFieldAnnotations(List<T> annotations, FieldInfo field) {
    for (T annotation : annotations) {
      visitFieldAnnotation(annotation, field);
    }
  }

  /**
   * Handles the annotation when applied to a field of a specification.
   * The default implementation of this method throws an {@link InvalidSpecException}.
   * This method is not called by Spock directly, but only by the default implementation of
   * {@link #visitFieldAnnotations(List, FieldInfo)}, so if you have overwritten that method,
   * there is no need to overwrite this one too except if you want to call it yourself.
   *
   * @param annotation the annotation found on the field
   * @param field      the annotated field
   */
  default void visitFieldAnnotation(T annotation, FieldInfo field) {
    throw new InvalidSpecException("@%s may not be applied to fields")
      .withArgs(annotation.annotationType().getSimpleName());
  }

  /**
   * Handles the annotation when applied to a fixture method of a specification one or multiple times.
   * The default implementation of this method calls {@link #visitFixtureAnnotation(Annotation, MethodInfo)}
   * once for each given annotation.
   *
   * @since 2.0
   * @param annotations   the annotations found on the fixture method
   * @param fixtureMethod the annotated fixture method
   */
  default void visitFixtureAnnotations(List<T> annotations, MethodInfo fixtureMethod) {
    for (T annotation : annotations) {
      visitFixtureAnnotation(annotation, fixtureMethod);
    }
  }

  /**
   * Handles the annotation when applied to a fixture method of a specification.
   * The default implementation of this method throws an {@link InvalidSpecException}.
   * This method is not called by Spock directly, but only by the default implementation of
   * {@link #visitFixtureAnnotations(List, MethodInfo)}, so if you have overwritten that method,
   * there is no need to overwrite this one too except if you want to call it yourself.
   *
   * @param annotation    the annotation found on the fixture method
   * @param fixtureMethod the annotated fixture method
   */
  default void visitFixtureAnnotation(T annotation, MethodInfo fixtureMethod) {
    throw new InvalidSpecException("@%s may not be applied to fixture methods")
      .withArgs(annotation.annotationType().getSimpleName());
  }

  /**
   * Handles the annotation when applied to a feature method of a specification one or multiple times.
   * The default implementation of this method calls {@link #visitFeatureAnnotation(Annotation, FeatureInfo)}
   * once for each given annotation.
   *
   * @since 2.0
   * @param annotations the annotations found on the feature method
   * @param feature     the annotated feature method
   */
  default void visitFeatureAnnotations(List<T> annotations, FeatureInfo feature) {
    for (T annotation : annotations) {
      visitFeatureAnnotation(annotation, feature);
    }
  }

  /**
   * Handles the annotation when applied to a feature method of a specification.
   * The default implementation of this method throws an {@link InvalidSpecException}.
   * This method is not called by Spock directly, but only by the default implementation of
   * {@link #visitFeatureAnnotations(List, FeatureInfo)}, so if you have overwritten that method,
   * there is no need to overwrite this one too except if you want to call it yourself.
   *
   * @param annotation the annotation found on the feature method
   * @param feature    the annotated feature method
   */
  default void visitFeatureAnnotation(T annotation, FeatureInfo feature) {
    throw new InvalidSpecException("@%s may not be applied to feature methods")
      .withArgs(annotation.annotationType().getSimpleName());
  }

  /**
   * Does concluding actions after the single annotations were handled.
   * This method is called after all the other {@code visit...} methods of this interface to do any concluding
   * or combining actions that are necessary. It is called once for each extension that has at least one
   * of its annotation somewhere on the given specification.
   *
   * @param spec the annotated specification
   */
  default void visitSpec(SpecInfo spec) {
  }
}
