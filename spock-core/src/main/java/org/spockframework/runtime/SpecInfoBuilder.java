/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.util.*;

import spock.lang.Specification;

/**
 * Builds a SpecInfo from a Class instance.
 *
 * @author Peter Niederwieser
 */
public class SpecInfoBuilder {
  private final Class<?> clazz;
  private final List<IGlobalExtension> globalExtensions;
  private final Map<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension> localExtensions =
    new HashMap<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension>();
  private final SpecInfo spec = new SpecInfo();

  public SpecInfoBuilder(Class<?> clazz, List<IGlobalExtension> globalExtensions) {
    this.clazz = clazz;
    this.globalExtensions = globalExtensions;
  }

  public SpecInfo build() {
    doBuild();
    int executionOrder = 0;
    for (SpecInfo curr : spec.getSpecsTopToBottom())
      for (FeatureInfo feature : curr.getFeatures())
        feature.setExecutionOrder(executionOrder++);

    return spec;
  }

  private SpecInfo doBuild() {
    buildSuperSpec();
    buildSpec();
    buildFields();
    buildSharedInstanceField();
    buildFeatures();
    buildFixtureMethods();
    return spec;
  }

  // TODO: move this to a more appropriate location (class)
  public void runExtensions() {
    runGlobalExtensions();
    runAnnotationDrivenExtensions();
  }

  private void buildSuperSpec() {
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == Object.class || superClass == Specification.class) return;

    SpecInfo superSpec = new SpecInfoBuilder(superClass, globalExtensions).doBuild();
    spec.setSuperSpec(superSpec);
    superSpec.setSubSpec(spec);
  }

  private void buildSpec() {
    SpecUtil.checkIsSpec(clazz);
    spec.setParent(null);
    spec.setName(clazz.getSimpleName());
    spec.setReflection(clazz);
    spec.setFilename(clazz.getAnnotation(SpecMetadata.class).filename());
  }

  private void buildFields() {
    for (Field field : clazz.getDeclaredFields()) {
      FieldMetadata metadata = field.getAnnotation(FieldMetadata.class);
      if (metadata == null) continue;

      FieldInfo fieldInfo = new FieldInfo();
      fieldInfo.setParent(spec);
      fieldInfo.setReflection(field);
      fieldInfo.setName(metadata.name());
      fieldInfo.setOrdinal(metadata.ordinal());
      fieldInfo.setLine(metadata.line());
      spec.addField(fieldInfo);
    }

    Collections.sort(spec.getFields(), new Comparator<FieldInfo>() {
      public int compare(FieldInfo f1, FieldInfo f2) {
        return f1.getOrdinal() - f2.getOrdinal();
      }
    });
  }

  private void buildSharedInstanceField() {
    Field field = getSharedInstanceField();
    FieldInfo fieldInfo = new FieldInfo();
    fieldInfo.setParent(spec);
    fieldInfo.setName(field.getName());
    fieldInfo.setReflection(field);
    spec.setSharedInstanceField(fieldInfo);
  }

  private Field getSharedInstanceField() {
    try {
      return clazz.getField(InternalIdentifiers.SHARED_INSTANCE_NAME);
    } catch (NoSuchFieldException e) {
      throw new InternalSpockError("cannot find shared instance field");
    }
  }

  private void buildFeatures() {
    for (Method method : clazz.getDeclaredMethods()) {
      FeatureMetadata metadata = method.getAnnotation(FeatureMetadata.class);
      if (metadata == null) continue;
      method.setAccessible(true);
      spec.addFeature(createFeature(method, metadata));
    }

    spec.sortFeatures(new IFeatureSortOrder() {
      public int compare(FeatureInfo m1, FeatureInfo m2) {
        return m1.getDeclarationOrder() - m2.getDeclarationOrder();
      }
    });
  }

  private FeatureInfo createFeature(Method method, FeatureMetadata featureMetadata) {
    FeatureInfo feature = new FeatureInfo();
    feature.setParent(spec);
    feature.setName(featureMetadata.name());
    feature.setDeclarationOrder(featureMetadata.ordinal());
    for (String name : featureMetadata.parameterNames())
      feature.addParameterName(name);

    MethodInfo featureMethod = new MethodInfo();
    featureMethod.setParent(spec);
    featureMethod.setName(featureMetadata.name());
    featureMethod.setFeature(feature);
    featureMethod.setReflection(method);
    featureMethod.setKind(MethodKind.FEATURE);
    feature.setFeatureMethod(featureMethod);

    String processorMethodName = InternalIdentifiers.getDataProcessorName(method.getName());
    MethodInfo dataProcessorMethod = createMethod(processorMethodName, MethodKind.DATA_PROCESSOR, false);

    if (dataProcessorMethod != null) {
      feature.setDataProcessorMethod(dataProcessorMethod);
      int providerCount = 0;
      String providerMethodName = InternalIdentifiers.getDataProviderName(method.getName(), providerCount++);
      MethodInfo providerMethod = createMethod(providerMethodName, MethodKind.DATA_PROVIDER, false);
      while (providerMethod != null) {
        feature.addDataProvider(createDataProvider(feature, providerMethod));
        providerMethodName = InternalIdentifiers.getDataProviderName(method.getName(), providerCount++);
        providerMethod = createMethod(providerMethodName, MethodKind.DATA_PROVIDER, false);
      }
    }

    for (BlockMetadata blockMetadata : featureMetadata.blocks()) {
      BlockInfo block = new BlockInfo();
      block.setKind(blockMetadata.kind());
      block.setTexts(Arrays.asList(blockMetadata.texts()));
      feature.addBlock(block);
    }

    return feature;
  }

  private DataProviderInfo createDataProvider(FeatureInfo feature, MethodInfo method) {
    DataProviderMetadata metadata = method.getReflection().getAnnotation(DataProviderMetadata.class);

    DataProviderInfo provider = new DataProviderInfo();
    provider.setParent(feature);
    provider.setLine(metadata.line());
    provider.setColumn(metadata.column());
    provider.setDataVariables(Arrays.asList(metadata.dataVariables()));
    provider.setDataProviderMethod(method);
    return provider;
  }

  private MethodInfo createMethod(String name, MethodKind kind, boolean allowStub) {
    Method reflection = findMethod(name);
    if (reflection == null && !allowStub) return null;

    MethodInfo methodInfo = new MethodInfo();
    methodInfo.setParent(spec);
    methodInfo.setName(name);
    methodInfo.setReflection(reflection);
    methodInfo.setKind(kind);
    return methodInfo;
  }

  private Method findMethod(String name) {
    for (Method method : spec.getReflection().getDeclaredMethods())
      if (method.getName().equals(name)) {
        method.setAccessible(true);
        return method;
      }
    return null;
  }

  private void buildFixtureMethods() {
    spec.setSetupMethod(createMethod(Identifiers.SETUP_METHOD, MethodKind.SETUP, true));
    spec.setCleanupMethod(createMethod(Identifiers.CLEANUP_METHOD, MethodKind.CLEANUP, true));
    spec.setSetupSpecMethod(createMethod(Identifiers.SETUP_SPEC_METHOD, MethodKind.SETUP_SPEC, true));
    spec.setCleanupSpecMethod(createMethod(Identifiers.CLEANUP_SPEC_METHOD, MethodKind.CLEANUP_SPEC, true));
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

    doRunAnnotationDrivenExtensions(spec.getSetupSpecMethod());
    doRunAnnotationDrivenExtensions(spec.getSetupMethod());
    doRunAnnotationDrivenExtensions(spec.getCleanupMethod());
    doRunAnnotationDrivenExtensions(spec.getCleanupSpecMethod());

    for (FeatureInfo feature : spec.getFeatures())
      doRunAnnotationDrivenExtensions(feature.getFeatureMethod());
  }

  @SuppressWarnings("unchecked")
  private void doRunAnnotationDrivenExtensions(NodeInfo<?, ?> node) {
    if (node.isStub()) return;

    for (Annotation ann : node.getReflection().getAnnotations()) {
      ExtensionAnnotation extAnn = ann.annotationType().getAnnotation(ExtensionAnnotation.class);
      if (extAnn == null) continue;
      IAnnotationDrivenExtension extension = getOrCreateExtension(extAnn.value());
      if (node instanceof SpecInfo)
        extension.visitSpecAnnotation(ann, (SpecInfo)node);
      else if (node instanceof MethodInfo) {
        MethodInfo method = (MethodInfo)node;
        if (method.getKind() == MethodKind.FEATURE)
          extension.visitFeatureAnnotation(ann, method.getFeature());
        else
          extension.visitFixtureAnnotation(ann, method);
      } else extension.visitFieldAnnotation(ann, (FieldInfo)node);
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
