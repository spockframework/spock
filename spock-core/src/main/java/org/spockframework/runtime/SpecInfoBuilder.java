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

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;

/**
 * Builds a SpecInfo from a Class instance.
 *
 * @author Peter Niederwieser
 */
public class SpecInfoBuilder {
  private final Class<?> clazz;
  private final SpecInfo spec = new SpecInfo();

  public SpecInfoBuilder(Class<?> clazz) {
    this.clazz = clazz;
  }

  public SpecInfo build() {
    doBuild();
    int order = 0;
    for (SpecInfo curr : spec.getSpecsTopToBottom())
      for (FeatureInfo feature : curr.getFeatures()) {
        feature.setDeclarationOrder(order); // turn into "global" order that considers whole inheritance chain
        feature.setExecutionOrder(order);
        order++;
      }

    return spec;
  }

  private SpecInfo doBuild() {
    buildSuperSpec();
    buildSpec();
    buildFields();
    buildFeatures();
    buildInitializerMethods();
    buildFixtureMethods();
    return spec;
  }

  private void buildSuperSpec() {
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == Object.class || superClass == Specification.class) return;

    SpecInfo superSpec = new SpecInfoBuilder(superClass).doBuild();
    spec.setSuperSpec(superSpec);
    superSpec.setSubSpec(spec);
  }

  private void buildSpec() {
    SpecUtil.checkIsSpec(clazz);

    SpecMetadata metadata = clazz.getAnnotation(SpecMetadata.class);
    spec.setParent(null);
    spec.setPackage(ReflectionUtil.getPackageName(clazz));
    spec.setName(clazz.getSimpleName());
    spec.setLine(metadata.line());
    spec.setReflection(clazz);
    spec.setFilename(metadata.filename());
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
      fieldInfo.setHasInitializer(metadata.initializer());
      spec.addField(fieldInfo);
    }

    spec.getFields().sort(comparingInt(FieldInfo::getOrdinal));
  }

  private void buildFeatures() {
    for (Method method : clazz.getDeclaredMethods()) {
      FeatureMetadata metadata = method.getAnnotation(FeatureMetadata.class);
      if (metadata == null) continue;
      method.setAccessible(true);
      spec.addFeature(createFeature(method, metadata));
    }

    spec.getFeatures().sort(comparingInt(FeatureInfo::getDeclarationOrder));
  }

  private FeatureInfo createFeature(Method method, FeatureMetadata featureMetadata) {
    FeatureInfo feature = new FeatureInfo();
    feature.setParent(spec);
    feature.setName(featureMetadata.name());
    feature.setLine(featureMetadata.line());
    feature.setDeclarationOrder(featureMetadata.ordinal());
    for (String name : featureMetadata.parameterNames())
      feature.addParameterName(name);

    MethodInfo featureMethod = new MethodInfo();
    featureMethod.setParent(spec);
    featureMethod.setName(featureMetadata.name());
    featureMethod.setLine(featureMetadata.line());
    featureMethod.setFeature(feature);
    featureMethod.setReflection(method);
    featureMethod.setKind(MethodKind.FEATURE);
    feature.setFeatureMethod(featureMethod);

    String processorMethodName = InternalIdentifiers.getDataProcessorName(method.getName());
    MethodInfo dataProcessorMethod = createMethod(processorMethodName, MethodKind.DATA_PROCESSOR);

    if (dataProcessorMethod != null) {
      feature.setDataProcessorMethod(dataProcessorMethod);

      DataProcessorMetadata dataProcessorMetadata = dataProcessorMethod.getAnnotation(DataProcessorMetadata.class);
      for (String dataVariable : dataProcessorMetadata.dataVariables()) {
        feature.addDataVariable(dataVariable);
      }

      int providerCount = 0;
      String providerMethodName = InternalIdentifiers.getDataProviderName(method.getName(), providerCount++);
      MethodInfo providerMethod = createMethod(providerMethodName, MethodKind.DATA_PROVIDER);
      while (providerMethod != null) {
        feature.addDataProvider(createDataProvider(feature, providerMethod));
        providerMethodName = InternalIdentifiers.getDataProviderName(method.getName(), providerCount++);
        providerMethod = createMethod(providerMethodName, MethodKind.DATA_PROVIDER);
      }
    }

    for (BlockMetadata blockMetadata : featureMetadata.blocks()) {
      BlockInfo block = new BlockInfo();
      block.setKind(blockMetadata.kind());
      block.setTexts(asList(blockMetadata.texts()));
      feature.addBlock(block);
    }

    return feature;
  }

  private DataProviderInfo createDataProvider(FeatureInfo feature, MethodInfo method) {
    DataProviderMetadata metadata = method.getAnnotation(DataProviderMetadata.class);

    DataProviderInfo provider = new DataProviderInfo();
    provider.setParent(feature);
    provider.setLine(metadata.line());
    provider.setDataVariables(asList(metadata.dataVariables()));
    provider.setPreviousDataTableVariables(asList(metadata.previousDataTableVariables()));
    provider.setDataProviderMethod(method);
    return provider;
  }

  @Nullable
  private MethodInfo createMethod(String name, MethodKind kind) {
    Method reflection = findMethod(name);
    if (reflection == null) return null;
    return createMethod(reflection, kind, name);
  }

  private MethodInfo createMethod(Method method, MethodKind kind, String name) {
    MethodInfo methodInfo = new MethodInfo();
    methodInfo.setParent(spec);
    methodInfo.setName(name);
    methodInfo.setReflection(method);
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

  private void buildInitializerMethods() {
    MethodInfo initializerMethod = createMethod(InternalIdentifiers.INITIALIZER_METHOD, MethodKind.INITIALIZER);
    if (initializerMethod != null) spec.setInitializerMethod(initializerMethod);
    MethodInfo sharedInitializerMethod = createMethod(InternalIdentifiers.SHARED_INITIALIZER_METHOD, MethodKind.SHARED_INITIALIZER);
    if (sharedInitializerMethod != null) spec.setSharedInitializerMethod(sharedInitializerMethod);
  }

  private void buildFixtureMethods() {
    MethodInfo cleanupMethod = createMethod(Identifiers.CLEANUP_METHOD, MethodKind.CLEANUP);
    if (cleanupMethod != null) spec.addCleanupMethod(cleanupMethod);
    MethodInfo cleanupSpecMethod = createMethod(Identifiers.CLEANUP_SPEC_METHOD, MethodKind.CLEANUP_SPEC);
    if (cleanupSpecMethod != null) spec.addCleanupSpecMethod(cleanupSpecMethod);

    MethodInfo setupMethod = createMethod(Identifiers.SETUP_METHOD, MethodKind.SETUP);
    if (setupMethod != null) spec.addSetupMethod(setupMethod);
    MethodInfo setupSpecMethod = createMethod(Identifiers.SETUP_SPEC_METHOD, MethodKind.SETUP_SPEC);
    if (setupSpecMethod != null) spec.addSetupSpecMethod(setupSpecMethod);
  }
}
