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

package org.spockframework.runtime.model;

import java.lang.reflect.AnnotatedElement;

/**
 * Runtime information about an iteration of a feature method.
 */
public class IterationInfo extends NodeInfo<FeatureInfo, AnnotatedElement> {
  private final Object[] dataValues;
  private final int estimatedNumIterations;

  public IterationInfo(FeatureInfo feature, Object[] dataValues, int estimatedNumIterations) {
    setParent(feature);
    this.dataValues = dataValues;
    this.estimatedNumIterations = estimatedNumIterations;
  }

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  /**
   * Return this iteration's data values for the ongoing execution of the
   * owning feature method. Information about the feature's data variables
   * is available through {@link FeatureInfo#getDataProviders}.
   * 
   * @return this iteration's data values for the ongoing execution of the
   * owning feature method
   */
  public Object[] getDataValues() {
    return dataValues;
  }

  /**
   * Returns the estimated total number of iterations for the ongoing execution
   * of the owning feature method. The value is obtained by calling
   * <tt>size()</tt> on each data provider before the first iteration is run.
   * It is only an estimate and won't change during feature execution (i.e. all
   * <tt>FeatureInfo<tt>s will return the same value).
   *
   * @return the estimated total number of iterations for the execution
   * of the owning feature method
   */
  public int getEstimatedNumIterations() {
    return estimatedNumIterations;
  }
}
