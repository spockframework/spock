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

package org.spockframework.runtime;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.IoUtil;
import org.spockframework.util.SpockUserHomeUtil;

public class SpecRunHistory implements Comparable<SpecRunHistory> {
  private static final int MAX_CONFIDENCE = 5;

  private final String specName;
  private Data data = new Data();

  public SpecRunHistory(String specName) {
    this.specName = specName;
  }

  public String getSpecName() {
    return specName;
  }

  public void loadFromDisk() throws IOException {
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(getDataFile()));
    try {
      data = (Data) in.readObject();
    } catch (ClassNotFoundException e) {
      // in JDK 1.5, there is no IOException constructor that takes a cause
      IOException io = new IOException("deserialization error");
      io.initCause(e);
      throw io;
    } finally {
      IoUtil.closeQuietly(in);
    }
  }

  public void saveToDisk() throws IOException {
    File file = getDataFile();
    IoUtil.createDirectory(file.getParentFile());

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    try {
      out.writeObject(data);
    } finally {
      IoUtil.closeQuietly(out);
    }
  }

  public void sortFeatures(SpecInfo spec) {
    spec.sortFeatures(new IFeatureSortOrder() {
      public int compare(FeatureInfo f1, FeatureInfo f2) {
        Integer confidence1 = data.featureConfidences.get(f1.getName());
        if (confidence1 == null) return -1;

        Integer confidence2 = data.featureConfidences.get(f2.getName());
        if (confidence2 == null) return 1;

        if (!confidence1.equals(confidence2))
          return confidence1 - confidence2;

        long duration1 = data.featureDurations.get(f1.getName()); // never null
        long duration2 = data.featureDurations.get(f2.getName()); // never null
        return duration1 < duration2 ? -1 : 1;
      }
    });
  }

  public int compareTo(SpecRunHistory other) {
    int confidenceDiff = data.specConfidence.compareTo(other.data.specConfidence);
    if (confidenceDiff != 0) return confidenceDiff;
    return data.specDuration < other.data.specDuration ? -1 : 1;
  }

  public void collectFeatureData(FeatureInfo feature, long duration, boolean failed) {
    data.featureDurations.put(feature.getName(), duration);

    Integer oldConfidence = data.featureConfidences.get(feature.getName());
    if (oldConfidence == null) oldConfidence = 0;
    int newConfidence = failed ? 0 : Math.min(MAX_CONFIDENCE, oldConfidence + 1);
    data.featureConfidences.put(feature.getName(), newConfidence);
  }

  public void collectSpecData(SpecInfo spec, long duration) {
    data.specDuration = duration;
    removeObsoleteFeaturesFromData(spec);
    computeSpecConfidence();
  }

  private void removeObsoleteFeaturesFromData(SpecInfo spec) {
    List<FeatureInfo> features = spec.getAllFeatures();
    Set<String> featureNames = extractNames(features);
    data.featureConfidences.keySet().retainAll(featureNames);
    data.featureDurations.keySet().retainAll(featureNames);
  }

  private void computeSpecConfidence() {
    int totalConfidence = 0;
    for (int confidence : data.featureConfidences.values())
      totalConfidence += confidence;

    int numFeatures = data.featureConfidences.size();
    data.specConfidence = numFeatures == 0 ? new BigDecimal(0) :
        new BigDecimal(totalConfidence).divide(new BigDecimal(numFeatures), MathContext.DECIMAL32);
  }

  private Set<String> extractNames(List<FeatureInfo> features) {
    Set<String> featureNames = new HashSet<String>(features.size());
    for (FeatureInfo feature : features)
      featureNames.add(feature.getName());
    return featureNames;
  }

  private File getDataFile() {
    return SpockUserHomeUtil.getFileInSpockUserHome("RunHistory", specName);
  }

  private static class Data implements Serializable {
    // BigDecimal ensures that specs with equal confidence will be ordered
    // according to their duration, instead of falling prey to some rounding error
    BigDecimal specConfidence = new BigDecimal(0);
    long specDuration = 0;

    Map<String, Integer> featureConfidences = new HashMap<String, Integer>();
    Map<String, Long> featureDurations = new HashMap<String, Long>();
  }
}

