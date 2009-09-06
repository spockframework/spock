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

import org.spockframework.compiler.Identifiers;
import org.spockframework.runtime.intercept.Directive;
import org.spockframework.runtime.intercept.IDirectiveProcessor;
import org.spockframework.runtime.model.*;
import org.spockframework.runtime.extension.ISpockExtension;
import org.spockframework.runtime.extension.ExtensionRegistry;
import org.spockframework.util.BinaryNames;

import spock.lang.Specification;

/**
 * Builds a SpeckInfo from a Class instance.
 *
 * @author Peter Niederwieser
 */
public class SpeckInfoBuilder {
  private final Class<?> clazz;
  private final Map<Class<? extends IDirectiveProcessor>, IDirectiveProcessor> processors =
    new HashMap<Class<? extends IDirectiveProcessor>, IDirectiveProcessor>();
  private final SpeckInfo speck = new SpeckInfo();

  public SpeckInfoBuilder(Class<?> clazz) {
    this.clazz = clazz;
  }

  public static boolean isSpecification(Class<?> clazz) {
    return clazz.isAnnotationPresent(SpeckMetadata.class);
  }
  
  public SpeckInfo build() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
    buildSuperSpeck();
    buildSpeck();
    buildFields();
    buildSharedInstanceField();
    buildFeatures();
    buildFixtureMethods();
    notifyExtensions();
    processDirectives();
    return speck;
  }

  private void buildSuperSpeck() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == Object.class || superClass == Specification.class) return;

    speck.setSuperSpeck(new SpeckInfoBuilder(superClass).build());  
  }

  private void buildSpeck() {
    SpeckMetadata metadata = getSpeckMetadata();
    speck.setParent(null);
    speck.setName(clazz.getSimpleName());
    speck.setReflection(clazz);
    speck.setFilename(metadata.filename());
  }

  private SpeckMetadata getSpeckMetadata() {
    SpeckMetadata metadata = clazz.getAnnotation(SpeckMetadata.class);
    if (metadata == null)
      throw new InvalidSpeckError(
          "Class '%s' is not a Speck, or has not been compiled properly").format(clazz.getName());

    return metadata;
  }

  private void buildFields() {
    for (Field field : clazz.getDeclaredFields()) {
      FieldMetadata metadata = field.getAnnotation(FieldMetadata.class);
      if (metadata == null) continue;

      FieldInfo fieldInfo = new FieldInfo();
      fieldInfo.setParent(speck);
      fieldInfo.setName(field.getName());
      fieldInfo.setReflection(field);
      fieldInfo.setOrdinal(metadata.ordinal());
      speck.addField(fieldInfo);
    }

    Collections.sort(speck.getFields(), new Comparator<FieldInfo>() {
      public int compare(FieldInfo f1, FieldInfo f2) {
        return f1.getOrdinal() - f2.getOrdinal();
      }
    });
  }

  private void buildSharedInstanceField() throws NoSuchFieldException {
    Field field = clazz.getField(Identifiers.SHARED_INSTANCE_NAME);

    FieldInfo fieldInfo = new FieldInfo();
    fieldInfo.setParent(speck);
    fieldInfo.setName(field.getName());
    fieldInfo.setReflection(field);
    speck.setSharedInstanceField(fieldInfo);
  }

  private void buildFeatures() {
    for (Method method : clazz.getDeclaredMethods()) {
      FeatureMetadata metadata = method.getAnnotation(FeatureMetadata.class);
      if (metadata == null) continue;
      method.setAccessible(true);
      speck.addFeature(createFeature(method, metadata));
    }

    speck.sortFeatures(new IFeatureSortOrder() {
      public int compare(FeatureInfo m1, FeatureInfo m2) {
        return m1.getOrdinal() - m2.getOrdinal();
      }
    });
  }

  private FeatureInfo createFeature(Method method, FeatureMetadata featureMetadata) {
    FeatureInfo feature = new FeatureInfo();
    feature.setParent(speck);
    feature.setName(featureMetadata.name());
    feature.setOrdinal(featureMetadata.ordinal());
    for (String name : featureMetadata.parameterNames())
      feature.addParameterName(name);

    MethodInfo featureMethod = new MethodInfo();
    featureMethod.setParent(speck);
    featureMethod.setName(featureMetadata.name());
    featureMethod.setFeature(feature);
    featureMethod.setReflection(method);
    featureMethod.setKind(MethodKind.FEATURE);
    feature.setFeatureMethod(featureMethod);

    String processorMethodName = BinaryNames.getDataProcessorName(method.getName());
    MethodInfo dataProcessorMethod = createMethod(processorMethodName, MethodKind.DATA_PROCESSOR, false);

    if (dataProcessorMethod != null) {
      feature.setDataProcessorMethod(dataProcessorMethod);
      int providerCount = 0;
      String providerMethodName = BinaryNames.getDataProviderName(method.getName(), providerCount++);
      MethodInfo providerMethod = createMethod(providerMethodName, MethodKind.DATA_PROVIDER, false);
      while (providerMethod != null) {
        feature.addDataProvider(createDataProvider(feature, providerMethod));
        providerMethodName = BinaryNames.getDataProviderName(method.getName(), providerCount++);
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
    methodInfo.setParent(speck);
    methodInfo.setName(name);
    methodInfo.setReflection(reflection);
    methodInfo.setKind(kind);
    return methodInfo;
  }

  private Method findMethod(String name) {
    for (Method method : speck.getReflection().getDeclaredMethods())
      if (method.getName().equals(name)) {
        method.setAccessible(true);
        return method;
      }
    return null;
  }

  private void buildFixtureMethods() {
    speck.setSetupMethod(createMethod(Identifiers.SETUP_METHOD, MethodKind.SETUP, true));
    speck.setCleanupMethod(createMethod(Identifiers.CLEANUP_METHOD, MethodKind.CLEANUP, true));
    speck.setSetupSpeckMethod(createMethod(Identifiers.SETUP_SPECK_METHOD, MethodKind.SETUP_SPECK, true));
    speck.setCleanupSpeckMethod(createMethod(Identifiers.CLEANUP_SPECK_METHOD, MethodKind.CLEANUP_SPECK, true));
  }

  private void notifyExtensions() {
    for (ISpockExtension extension : ExtensionRegistry.getInstance().getExtensions())
      extension.visitSpeck(speck);
  }

  private void processDirectives() throws InstantiationException, IllegalAccessException {
    processNodeDirective(speck);
    for (FieldInfo field : speck.getFields())
      processNodeDirective(field);
    processNodeDirective(speck.getSetupSpeckMethod());
    processNodeDirective(speck.getSetupMethod());
    processNodeDirective(speck.getCleanupMethod());
    processNodeDirective(speck.getCleanupSpeckMethod());
    for (FeatureInfo feature : speck.getFeatures())
      processNodeDirective(feature.getFeatureMethod());
    for (IDirectiveProcessor processor : processors.values())
      processor.afterVisits(speck);
  }

  @SuppressWarnings("unchecked")
  private void processNodeDirective(NodeInfo node) throws InstantiationException, IllegalAccessException {
    if (node.isStub()) return;

    for (Annotation ann : node.getReflection().getDeclaredAnnotations()) {
      Directive directive = ann.annotationType().getAnnotation(Directive.class);
      if (directive == null) continue;
      IDirectiveProcessor processor = getOrCreateProcessor(directive.value());
      if (node instanceof SpeckInfo)
        processor.visitSpeckDirective(ann, (SpeckInfo)node);
      else if (node instanceof MethodInfo) {
        MethodInfo method = (MethodInfo)node;
        if (method.getKind() == MethodKind.FEATURE)
          processor.visitFeatureDirective(ann, method.getFeature());
        else
          processor.visitFixtureDirective(ann, method);
      } else processor.visitFieldDirective(ann, (FieldInfo)node);
    }
  }

  private IDirectiveProcessor getOrCreateProcessor(Class<? extends IDirectiveProcessor> clazz)
    throws InstantiationException, IllegalAccessException {
    IDirectiveProcessor result = processors.get(clazz);
    if (result == null) {
      result = clazz.newInstance();
      processors.put(clazz, result);
    }
    return result;
  }
}
