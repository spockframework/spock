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

import org.spockframework.compiler.Constants;
import org.spockframework.runtime.intercept.Directive;
import org.spockframework.runtime.intercept.IDirectiveProcessor;
import org.spockframework.runtime.model.*;
import org.spockframework.util.BinaryNames;
import org.spockframework.util.InternalSpockError;
import spock.lang.Speck;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds a SpeckInfo from a Class instance.
 *
 * @author Peter Niederwieser
 */
public class SpeckInfoBuilder {
  private final Class<?> clazz;
  private final Map<Class<? extends IDirectiveProcessor>, IDirectiveProcessor> processors =
    new HashMap<Class<? extends IDirectiveProcessor>, IDirectiveProcessor>();
  private SpeckInfo speck;

  public SpeckInfoBuilder(Class<?> clazz) {
    this.clazz = clazz;
  }

  public SpeckInfo build() throws InstantiationException, IllegalAccessException {
    checkIsSpeck();
    buildSpeck();
    buildFields();
    buildFeatures();
    buildLifecycleMethods();
    processDirectives();
    return speck;
  }

  private void checkIsSpeck() {
    Speck marker = clazz.getAnnotation(Speck.class);
    if (marker == null)
      throw new InvalidSpeckError(
          "Class '%s' is not a Speck (did you forget to add @Speck?)").withArgs(clazz.getName());

    SpeckMetadata metadata = clazz.getAnnotation(SpeckMetadata.class);
    if (metadata == null)
      // we know that Spock is on the compile classpath because @Speck is present
      // in the class file, but for some reason the Speck wasn't transformed
      throw new InternalSpockError("Speck '%s' has not been compiled properly").withArgs(clazz.getName());
  }

  private void buildSpeck() {
    speck = new SpeckInfo();
    speck.setParent(null);
    speck.setName(clazz.getSimpleName());
    speck.setReflection(clazz);
  }

  private void buildFields() {
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isSynthetic()) continue;
      FieldInfo fieldInfo = new FieldInfo();
      fieldInfo.setParent(speck);
      fieldInfo.setName(field.getName());
      fieldInfo.setReflection(field);
      speck.addField(fieldInfo);
    }
  }

  private void buildFeatures() {
    for (Method method : clazz.getDeclaredMethods()) {
      FeatureMetadata metadata = method.getAnnotation(FeatureMetadata.class);
      if (metadata == null) continue;
      speck.addFeature(createFeature(method, metadata));
    }

    speck.sortFeatures(new IFeatureSortOrder() {
      public int compare(FeatureInfo m1, FeatureInfo m2) {
        return m1.getOrder() - m2.getOrder();
      }
    });
  }

  private FeatureInfo createFeature(Method method, FeatureMetadata featureMetadata) {
    FeatureInfo feature = new FeatureInfo();
    feature.setParent(speck);
    feature.setName(featureMetadata.name());
    feature.setOrder(featureMetadata.order());
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
        feature.addDataProviderMethod(providerMethod);
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
      if (method.getName().equals(name)) return method;
    return null;
  }

  private void buildLifecycleMethods() {
    speck.setSetupMethod(createMethod(Constants.SETUP_METHOD, MethodKind.SETUP, true));
    speck.setCleanupMethod(createMethod(Constants.CLEANUP_METHOD, MethodKind.CLEANUP, true));
    speck.setSetupSpeckMethod(createMethod(Constants.SETUP_SPECK_METHOD, MethodKind.SETUP_SPECK, true));
    speck.setCleanupSpeckMethod(createMethod(Constants.CLEANUP_SPECK_METHOD, MethodKind.CLEANUP_SPECK, true));
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
  }

  private void processNodeDirective(NodeInfo node) throws InstantiationException, IllegalAccessException {
    if (node.isStub()) return;

    for (Annotation ann : node.getReflection().getDeclaredAnnotations()) {
      Directive directive = ann.annotationType().getAnnotation(Directive.class);
      if (directive == null) continue;
      IDirectiveProcessor processor = getOrCreateProcessor(directive.value());
      if (node instanceof SpeckInfo)
        processor.visitSpeckDirective(ann, (SpeckInfo)node);
      else if (node instanceof MethodInfo)
        processor.visitMethodDirective(ann, (MethodInfo)node);
      else processor.visitFieldDirective(ann, (FieldInfo)node);
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
