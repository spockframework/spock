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

import org.spockframework.dsl.*;
import org.spockframework.runtime.intercept.Directive;
import org.spockframework.runtime.intercept.IDirectiveProcessor;
import org.spockframework.runtime.model.*;
import org.spockframework.util.BinaryNames;
import org.spockframework.util.InternalSpockError;

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
    buildMethods();
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
    speck.setName(clazz.getSimpleName());
    speck.setParent(null);
    speck.setReflection(clazz);
  }

  private void buildFields() {
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isSynthetic()) continue;
      FieldInfo fieldInfo = new FieldInfo();
      fieldInfo.setName(field.getName());
      fieldInfo.setParent(speck);
      fieldInfo.setReflection(field);
      speck.addField(fieldInfo);
    }
  }

  private void buildMethods() {
    for (Method method : clazz.getDeclaredMethods()) {
      MethodMetadata methodAnn = method.getAnnotation(MethodMetadata.class);
      if (methodAnn == null) continue;
      speck.addMethod(createMethodInfo(method, methodAnn));
    }

    speck.sortFeatureMethods(new IMethodInfoSortOrder() {
      public int compare(MethodInfo m1, MethodInfo m2) {
        return m1.getIndex() - m2.getIndex();
      }
    });

    addStubIfMissing(speck.getSetupMethod(), MethodKind.SETUP);
    addStubIfMissing(speck.getCleanupMethod(), MethodKind.CLEANUP);
    addStubIfMissing(speck.getSetupSpeckMethod(), MethodKind.SETUP_SPECK);
    addStubIfMissing(speck.getCleanupSpeckMethod(), MethodKind.CLEANUP_SPECK);
  }

  private MethodInfo createMethodInfo(Method method, MethodMetadata methodAnn) {
    MethodInfo methodInfo = new MethodInfo();
    methodInfo.setIndex(methodAnn.index());
    methodInfo.setName(methodAnn.name());
    methodInfo.setKind(methodAnn.kind());
    methodInfo.setParent(speck);
    methodInfo.setReflection(method);

    if (methodInfo.getKind() == MethodKind.FEATURE) {
      String processorName = BinaryNames.getDataProcessorName(method.getName());
      MethodInfo dataProcessor = findMethod(processorName, MethodKind.DATA_PROCESSOR);

      if (dataProcessor != null) {
        methodInfo.setDataProcessor(dataProcessor);
        int providerCount = 0;
        String providerName = BinaryNames.getDataProviderName(method.getName(), providerCount++);
        MethodInfo provider = findMethod(providerName, MethodKind.DATA_PROVIDER);
        while (provider != null) {
          methodInfo.addDataProvider(provider);
          providerName = BinaryNames.getDataProviderName(method.getName(), providerCount++);
          provider = findMethod(providerName, MethodKind.DATA_PROVIDER);
        }
      }
    }

    for (BlockMetadata blockAnn : methodAnn.blocks()) {
      BlockInfo block = new BlockInfo();
      block.setKind(blockAnn.kind());
      block.setTexts(Arrays.asList(blockAnn.texts()));
      methodInfo.addBlock(block);
    }

    return methodInfo;
  }

  private MethodInfo findMethod(String name, MethodKind kind) {
    for (Method method : speck.getReflection().getDeclaredMethods()) {
      if (!method.getName().equals(name)) continue;

      MethodInfo methodInfo = new MethodInfo();
      methodInfo.setName(name);
      methodInfo.setParent(speck);
      methodInfo.setReflection(method);
      methodInfo.setIndex(-1);
      methodInfo.setKind(kind);
      return methodInfo;
    }

    return null;
  }

  // adds stubs for missing fixture methods so that directive processors can add interceptors
  private void addStubIfMissing(MethodInfo method, MethodKind kind) {
    if (method != null) return;

    method = new MethodInfo();
    method.setKind(kind);
    method.setParent(speck);
    speck.addMethod(method);
  }

  private void processDirectives() throws InstantiationException, IllegalAccessException {
    processNodeDirective(speck);
    for (FieldInfo field : speck.getFields())
      processNodeDirective(field);
    processNodeDirective(speck.getSetupSpeckMethod());
    processNodeDirective(speck.getSetupMethod());
    processNodeDirective(speck.getCleanupMethod());
    processNodeDirective(speck.getCleanupSpeckMethod());
    for (MethodInfo method : speck.getFeatureMethods())
      processNodeDirective(method);
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
