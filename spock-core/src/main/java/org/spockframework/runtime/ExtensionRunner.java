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

package org.spockframework.runtime;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;

import java.lang.annotation.Annotation;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Runs global and local extensions for a spec.
 */
public class ExtensionRunner {
  private final SpecInfo spec;
  private final IExtensionRegistry extensionRegistry;
  private final IConfigurationRegistry configurationRegistry;
  private final Map<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension> localExtensions =
    new HashMap<>();

  public ExtensionRunner(SpecInfo spec, IExtensionRegistry extensionRegistry, IConfigurationRegistry configurationRegistry) {
    this.spec = spec;
    this.extensionRegistry = extensionRegistry;
    this.configurationRegistry = configurationRegistry;
  }

  public void run() {
    runGlobalExtensions();
    runAnnotationDrivenExtensions();
  }

  private void runGlobalExtensions() {
    for (IGlobalExtension extension : extensionRegistry.getGlobalExtensions()) {
      extension.visitSpec(spec);
    }
  }

  public void runAnnotationDrivenExtensions() {
    runAnnotationDrivenExtensions(spec);

    for (IAnnotationDrivenExtension extension : localExtensions.values()) {
      extension.visitSpec(spec);
    }
  }

  public void runAnnotationDrivenExtensions(SpecInfo spec) {
    if (spec == null) return;
    runAnnotationDrivenExtensions(spec.getSuperSpec());

    doRunAnnotationDrivenExtensions(spec);

    for (FieldInfo field : spec.getFields()) {
      doRunAnnotationDrivenExtensions(field);
    }

    doRunAnnotationDrivenExtensions(spec.getSetupSpecMethods());
    doRunAnnotationDrivenExtensions(spec.getSetupMethods());
    doRunAnnotationDrivenExtensions(spec.getCleanupMethods());
    doRunAnnotationDrivenExtensions(spec.getCleanupSpecMethods());

    for (FeatureInfo feature : spec.getFeatures()) {
      doRunAnnotationDrivenExtensions(feature.getFeatureMethod());
    }
  }

  private void doRunAnnotationDrivenExtensions(Iterable<MethodInfo> nodes) {
    for (MethodInfo node : nodes) {
      doRunAnnotationDrivenExtensions(node);
    }
  }

  private void doRunAnnotationDrivenExtensions(NodeInfo<?, ?> node) {
    RepeatedExtensionAnnotations repeatedExtensionAnnotations = node.getAnnotation(RepeatedExtensionAnnotations.class);

    List<List<Annotation>> annotations;
    if (repeatedExtensionAnnotations == null) {
      annotations = Arrays
        .stream(node.getAnnotations())
        .map(Collections::singletonList)
        .collect(toList());
    } else {
      annotations = new ArrayList<>();
      List<Class<? extends Annotation>> repeatedAnnotations = asList(repeatedExtensionAnnotations.value());

      // add all direct annotations except those marked as repeated
      Arrays.stream(node.getAnnotations())
        .filter(annotation -> repeatedAnnotations
          .stream()
          .noneMatch(repeatedAnnotation -> repeatedAnnotation.isInstance(annotation)))
        .map(Collections::singletonList)
        .forEach(annotations::add);

      // add all annotations marked as repeated
      for (Class<? extends Annotation> repeatedAnnotation : repeatedAnnotations) {
        annotations.add(asList(node.getAnnotationsByType(repeatedAnnotation)));
      }
    }

    doRunAnnotationDrivenExtensions(node, annotations);
  }

  @SuppressWarnings("unchecked")
  private void doRunAnnotationDrivenExtensions(NodeInfo<?, ?> node, List<List<Annotation>> annotations) {
    for (List<Annotation> ann : annotations) {
      ExtensionAnnotation extAnn = ann.get(0).annotationType().getAnnotation(ExtensionAnnotation.class);
      if (extAnn == null) continue;
      IAnnotationDrivenExtension extension = getOrCreateExtension(extAnn.value());
      if (node instanceof SpecInfo) {
        extension.visitSpecAnnotations(ann, (SpecInfo) node);
      } else if (node instanceof MethodInfo) {
        MethodInfo method = (MethodInfo) node;
        if (method.getKind() == MethodKind.FEATURE) {
          extension.visitFeatureAnnotations(ann, method.getFeature());
        } else {
          extension.visitFixtureAnnotations(ann, method);
        }
      } else {
        extension.visitFieldAnnotations(ann, (FieldInfo) node);
      }
    }
  }

  private IAnnotationDrivenExtension getOrCreateExtension(Class<? extends IAnnotationDrivenExtension> clazz) {
    IAnnotationDrivenExtension extension = localExtensions.get(clazz);
    if (extension == null) {
      extension = configurationRegistry.instantiateExtension(clazz);
      localExtensions.put(clazz, extension);
    }
    return extension;
  }
}
