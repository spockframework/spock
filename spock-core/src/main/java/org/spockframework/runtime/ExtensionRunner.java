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

import java.lang.annotation.Annotation;
import java.util.*;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;

/**
 * Runs global and local extensions for a spec.
 */
public class ExtensionRunner {
  private final SpecInfo spec;
  private final List<IGlobalExtension> globalExtensions;
  private final Map<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension> localExtensions =
      new HashMap<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension>();

  public ExtensionRunner(SpecInfo spec, List<IGlobalExtension> globalExtensions) {
    this.spec = spec;
    this.globalExtensions = globalExtensions;
  }

  public void run() {
    runGlobalExtensions();
    runAnnotationDrivenExtensions();
  }

  private void runGlobalExtensions() {
    for (IGlobalExtension extension : globalExtensions)
      extension.visitSpec(spec);
  }

  public void runAnnotationDrivenExtensions() {
    runAnnotationDrivenExtensions(spec);

    for (IAnnotationDrivenExtension extension : localExtensions.values())
      extension.visitSpec(spec);
  }

  public void runAnnotationDrivenExtensions(SpecInfo spec) {
    if (spec == null) return;
    runAnnotationDrivenExtensions(spec.getSuperSpec());

    doRunAnnotationDrivenExtensions(spec);

    for (FieldInfo field : spec.getFields())
      doRunAnnotationDrivenExtensions(field);

    doRunAnnotationDrivenExtensions(spec.getSetupSpecMethods());
    doRunAnnotationDrivenExtensions(spec.getSetupMethods());
    doRunAnnotationDrivenExtensions(spec.getCleanupMethods());
    doRunAnnotationDrivenExtensions(spec.getCleanupSpecMethods());

    for (FeatureInfo feature : spec.getFeatures())
      doRunAnnotationDrivenExtensions(feature.getFeatureMethod());
  }

  private void doRunAnnotationDrivenExtensions(Iterable<MethodInfo> nodes) {
    for (MethodInfo node : nodes) doRunAnnotationDrivenExtensions(node);

  }

  @SuppressWarnings("unchecked")
  private void doRunAnnotationDrivenExtensions(NodeInfo<?, ?> node) {
    for (Annotation ann : node.getAnnotations()) {
      ExtensionAnnotation extAnn = ann.annotationType().getAnnotation(ExtensionAnnotation.class);
      if (extAnn == null) continue;
      IAnnotationDrivenExtension extension = getOrCreateExtension(extAnn.value());
      if (node instanceof SpecInfo)
        extension.visitSpecAnnotation(ann, (SpecInfo) node);
      else if (node instanceof MethodInfo) {
        MethodInfo method = (MethodInfo) node;
        if (method.getKind() == MethodKind.FEATURE)
          extension.visitFeatureAnnotation(ann, method.getFeature());
        else
          extension.visitFixtureAnnotation(ann, method);
      } else extension.visitFieldAnnotation(ann, (FieldInfo) node);
    }
  }

  private IAnnotationDrivenExtension getOrCreateExtension(Class<? extends IAnnotationDrivenExtension> clazz) {
    IAnnotationDrivenExtension result = localExtensions.get(clazz);
    if (result == null) {
      try {
        result = clazz.newInstance();
      } catch (InstantiationException e) {
        throw new ExtensionException("Failed to instantiate extension '%s'", e).withArgs(clazz);
      } catch (IllegalAccessException e) {
        throw new ExtensionException("No-arg constructor of extension '%s' is not public", e).withArgs(clazz);
      }
      localExtensions.put(clazz, result);
    }
    return result;
  }
}
