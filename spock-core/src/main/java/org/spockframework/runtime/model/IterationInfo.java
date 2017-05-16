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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime information about an iteration of a feature method.
 */
public class IterationInfo extends NodeInfo<FeatureInfo, AnnotatedElement> {
  private final Map<String, Object> dataValues;
  private final int estimatedNumIterations;
  private final List<Runnable> cleanups = new ArrayList<Runnable>();

  public IterationInfo(FeatureInfo feature, Map<String, Object> dataValues, int estimatedNumIterations) {
    setParent(feature);
    this.dataValues = new HashMap<String, Object>(dataValues);
    this.estimatedNumIterations = estimatedNumIterations;
  }

  public FeatureInfo getFeature() {
    return getParent();
  }

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  /**
   * Returns the name of this iteration. No strong guarantees are provided for this name,
   * except that it is non-null. For example, it may be the same as the feature name,
   * and it may not be unique among iterations of the same feature execution.
   * Nevertheless, this is generally the name that should be presented to the user (if any).
   *
   * @return the name of this iteration
   */
  public String getName() {
    return super.getName();
  }

  /**
   * Return this iteration's data values for the ongoing execution of the
   * owning feature method. The names of the data values (in the same order)
   * are available through {@link FeatureInfo#getDataVariables}.
   *
   * @return this iteration's data values for the ongoing execution of the
   * owning feature method
   */
  public Map<String, Object> getDataValues() {
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

  public void addCleanup(Runnable cleanup) {
    cleanups.add(cleanup);
  }

  public List<Runnable> getCleanups() {
    return cleanups;
  }
}
