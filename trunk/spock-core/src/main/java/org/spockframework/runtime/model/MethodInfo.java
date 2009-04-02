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

package org.spockframework.runtime.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.spockframework.runtime.intercept.IMethodInterceptor;

/**
 * Runtime information about a method in a Spock specification.
 * 
 * @author Peter Niederwieser
 */
public class MethodInfo extends NodeInfo<SpeckInfo, Method> {
  private int index;
  private MethodKind kind;
  private final List<BlockInfo> blocks = new ArrayList<BlockInfo>();
  private MethodInfo dataProcessor;
  private final List<MethodInfo> dataProviders = new ArrayList<MethodInfo>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<IMethodInterceptor>();

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public MethodKind getKind() {
    return kind;
  }

  public void setKind(MethodKind kind) {
    this.kind = kind;
  }

  public void addBlock(BlockInfo block) {
    blocks.add(block);
  }

  public List<BlockInfo> getBlocks() {
    return blocks;
  }

  public boolean isParameterized() {
    return dataProcessor != null;
  }

  public MethodInfo getDataProcessor() {
    return dataProcessor;
  }

  public void setDataProcessor(MethodInfo dataProcessor) {
    this.dataProcessor = dataProcessor;
  }

  public List<MethodInfo> getDataProviders() {
    return dataProviders;
  }

  public void addDataProvider(MethodInfo provider) {
    dataProviders.add(provider);
  }

  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  /**
   * Tells if this method, its data processor, or any of its data providers
   * has the specified name in bytecode.
   *
   * @param name a method name in bytecode
   * @return <tt>true</tt iff this method, its data processor, or any of its
   * data providers has the specified name in bytecode
   */
  public boolean isAssociatedWithBytecodeName(String name) {
    if (hasBytecodeName(name)) return true;
    if (dataProcessor != null && dataProcessor.hasBytecodeName(name)) return true;
    for (MethodInfo provider : dataProviders)
      if (provider.hasBytecodeName(name)) return true;
    return false;
  }

  public boolean hasBytecodeName(String name) {
    return getReflection().getName().equals(name);
  }
}
