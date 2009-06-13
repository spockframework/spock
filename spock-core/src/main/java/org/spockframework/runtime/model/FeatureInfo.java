package org.spockframework.runtime.model;

import org.spockframework.runtime.intercept.IMethodInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.AnnotatedElement;

/**
 * @author Peter Niederwieser
 */
public class FeatureInfo extends NodeInfo<SpeckInfo, AnnotatedElement> {
  private int order;
  private List<String> parameterNames = new ArrayList<String>();
  private final List<BlockInfo> blocks = new ArrayList<BlockInfo>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<IMethodInterceptor>();
  
  private MethodInfo featureMethod;
  private MethodInfo dataProcessorMethod;
  private final List<MethodInfo> dataProviderMethods = new ArrayList<MethodInfo>();

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public void addParameterName(String parameterName) {
    parameterNames.add(parameterName);
  }

  public List<BlockInfo> getBlocks() {
    return blocks;
  }

  public void addBlock(BlockInfo block) {
    blocks.add(block);
  }

  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public MethodInfo getFeatureMethod() {
    return featureMethod;
  }

  public void setFeatureMethod(MethodInfo method) {
    this.featureMethod = method;
  }

  public MethodInfo getDataProcessorMethod() {
    return dataProcessorMethod;
  }

  public void setDataProcessorMethod(MethodInfo method) {
    this.dataProcessorMethod = method;
  }

  public List<MethodInfo> getDataProviderMethods() {
    return dataProviderMethods;
  }

  public void addDataProviderMethod(MethodInfo method) {
    dataProviderMethods.add(method);
  }

  public boolean isParameterized() {
    return dataProcessorMethod != null;
  }

  /**
   * Tells if any of the methods associated with this feature has the specified
   * name in bytecode.
   *
   * @param name a method name in bytecode
   * @return <tt>true</tt iff any of the methods associated with this feature
   * has the specified name in bytecode
   */
  public boolean hasBytecodeName(String name) {
    if (featureMethod.hasBytecodeName(name)) return true;
    if (dataProcessorMethod != null && dataProcessorMethod.hasBytecodeName(name)) return true;
    for (MethodInfo provider : dataProviderMethods)
      if (provider.hasBytecodeName(name)) return true;
    return false;
  }
}
