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
  private final Map<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension> extensions =
    new HashMap<Class<? extends IAnnotationDrivenExtension>, IAnnotationDrivenExtension>();
  private final SpecInfo spec = new SpecInfo();

  public SpecInfoBuilder(Class<?> clazz) {
    this.clazz = clazz;
  }

  public SpecInfo build() {
    try {
      buildSuperSpec();
      buildSpec();
      buildFields();
      buildSharedInstanceField();
      buildFeatures();
      buildFixtureMethods();
      notifyGlobalExtensions();
      processAnnotationDrivenExtensions();
    } catch (InstantiationException e) {
      internalError(e, clazz);
    } catch (IllegalAccessException e) {
      internalError(e, clazz);
    } catch (NoSuchFieldException e) {
      internalError(e, clazz);
    }

    return spec;
  }

  private void internalError(Exception e, Class<?> clazz) {
    throw new InternalSpockError("Unexpected error while preparing execution of spec '%s'", e)
        .withArgs(clazz.getSimpleName());
  }

  private void buildSuperSpec() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == Object.class || superClass == Specification.class) return;

    spec.setSuperSpec(new SpecInfoBuilder(superClass).build());
  }

  private void buildSpec() {
    SpecMetadata metadata = getSpecMetadata();
    spec.setParent(null);
    spec.setName(clazz.getSimpleName());
    spec.setReflection(clazz);
    spec.setFilename(metadata.filename());
  }

  private SpecMetadata getSpecMetadata() {
    SpecMetadata metadata = clazz.getAnnotation(SpecMetadata.class);
    if (metadata == null)
      throw new InvalidSpecException(
          "Class '%s' is not a Spock specification").format(clazz.getName());

    return metadata;
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

  private void buildSharedInstanceField() throws NoSuchFieldException {
    Field field = clazz.getField(Identifiers.SHARED_INSTANCE_NAME);

    FieldInfo fieldInfo = new FieldInfo();
    fieldInfo.setParent(spec);
    fieldInfo.setName(field.getName());
    fieldInfo.setReflection(field);
    spec.setSharedInstanceField(fieldInfo);
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
        return m1.getOrdinal() - m2.getOrdinal();
      }
    });
  }

  private FeatureInfo createFeature(Method method, FeatureMetadata featureMetadata) {
    FeatureInfo feature = new FeatureInfo();
    feature.setParent(spec);
    feature.setName(featureMetadata.name());
    feature.setOrdinal(featureMetadata.ordinal());
    for (String name : featureMetadata.parameterNames())
      feature.addParameterName(name);

    MethodInfo featureMethod = new MethodInfo();
    featureMethod.setParent(spec);
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

  private void notifyGlobalExtensions() {
    for (IGlobalExtension extension : ExtensionRegistry.getInstance().getExtensions())
      extension.visitSpec(spec);
  }

  private void processAnnotationDrivenExtensions() throws InstantiationException, IllegalAccessException {
    processAnnotationDrivenExtensions(spec);
    for (FieldInfo field : spec.getFields())
      processAnnotationDrivenExtensions(field);
    processAnnotationDrivenExtensions(spec.getSetupSpecMethod());
    processAnnotationDrivenExtensions(spec.getSetupMethod());
    processAnnotationDrivenExtensions(spec.getCleanupMethod());
    processAnnotationDrivenExtensions(spec.getCleanupSpecMethod());
    for (FeatureInfo feature : spec.getFeatures())
      processAnnotationDrivenExtensions(feature.getFeatureMethod());
    for (IAnnotationDrivenExtension extension : extensions.values())
      extension.visitSpec(spec);
  }

  @SuppressWarnings("unchecked")
  private void processAnnotationDrivenExtensions(NodeInfo node) throws InstantiationException, IllegalAccessException {
    if (node.isStub()) return;

    for (Annotation ann : node.getReflection().getDeclaredAnnotations()) {
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

  private IAnnotationDrivenExtension getOrCreateExtension(Class<? extends IAnnotationDrivenExtension> clazz)
    throws InstantiationException, IllegalAccessException {
    IAnnotationDrivenExtension result = extensions.get(clazz);
    if (result == null) {
      result = clazz.newInstance();
      extensions.put(clazz, result);
    }
    return result;
  }
}
