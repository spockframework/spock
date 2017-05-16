package org.spockframework.runtime.model;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.util.Nullable;

/**
 * @author Peter Niederwieser
 */
public class FeatureInfo extends SpecElementInfo<SpecInfo, AnnotatedElement> {
  private int declarationOrder; // per spec class
  private int executionOrder;   // per spec inheritance chain

  private List<String> parameterNames = new ArrayList<String>();
  private final List<BlockInfo> blocks = new ArrayList<BlockInfo>();
  private final List<IMethodInterceptor> iterationInterceptors = new ArrayList<IMethodInterceptor>();

  private MethodInfo featureMethod;
  private MethodInfo dataProcessorMethod;
  private NameProvider<IterationInfo> iterationNameProvider;
  private final List<DataProviderInfo> dataProviders = new ArrayList<DataProviderInfo>();

  private boolean reportIterations = false;

  public SpecInfo getSpec() {
    return getParent();
  }

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  public int getDeclarationOrder() {
    return declarationOrder;
  }

  public void setDeclarationOrder(int declarationOrder) {
    this.declarationOrder = declarationOrder;
  }

  public int getExecutionOrder() {
    return executionOrder;
  }

  public void setExecutionOrder(int executionOrder) {
    this.executionOrder = executionOrder;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public void addParameterName(String parameterName) {
    parameterNames.add(parameterName);
  }

  public List<String> getDataVariables() {
    return parameterNames; // currently the same
  }

  public List<BlockInfo> getBlocks() {
    return blocks;
  }

  public void addBlock(BlockInfo block) {
    blocks.add(block);
  }

  public List<IMethodInterceptor> getIterationInterceptors() {
    return iterationInterceptors;
  }

  public void addIterationInterceptor(IMethodInterceptor interceptor) {
    iterationInterceptors.add(interceptor);
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

  public List<DataProviderInfo> getDataProviders() {
    return dataProviders;
  }

  public void addDataProvider(DataProviderInfo dataProvider) {
    dataProviders.add(dataProvider);
  }

  public boolean isParameterized() {
    return dataProcessorMethod != null;
  }

  public boolean isReportIterations() {
    return reportIterations;
  }

  public void setReportIterations(boolean flag) {
    reportIterations = flag;
  }

  @Nullable
  public NameProvider<IterationInfo> getIterationNameProvider() {
    return iterationNameProvider;
  }

  public void setIterationNameProvider(NameProvider<IterationInfo> provider) {
    iterationNameProvider = provider;
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
    for (DataProviderInfo provider : dataProviders)
      if (provider.getDataProviderMethod().hasBytecodeName(name)) return true;
    return false;
  }
}
