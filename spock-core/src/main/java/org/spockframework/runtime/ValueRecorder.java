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

import java.util.*;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.CollectionUtil;

/**
 * Records the values in a condition.
 *
 * @author Peter Niederwieser
 */
public class ValueRecorder implements Iterable<Object> {
  private final ArrayList<Object> values = new ArrayList<Object>();

  public static final String RESET = "reset";

  public ValueRecorder reset() {
    values.clear();
    return this;
  }

  public static final String RECORD = "record";
  
  /**
   * Records and returns the specified value. Hence an expression can be replaced
   * with record(expression) without impacting evaluation of the expression.
   */
  public Object record(int index, Object value) {
    realizeNas(index, null);
    values.add(value);
    return value;
  }

  public static final String REALIZE_NAS = "realizeNas";

  /**
   * Materializes N/A values without recording a new value.
   */
  public Object realizeNas(int index, Object value) {
    for (int i = values.size(); i < index; i++)
      values.add(ExpressionInfo.VALUE_NOT_AVAILABLE);
    return value;
  }

  /**
   * Returns an iterator over the recorded values.
   * @return an iterator over the recorded values
   */
  public Iterator<Object> iterator() {
    return values.iterator();
  }

  /**
   * Useful for manipulating the last recorded value.
   */
  public void replaceLastValue(Object newValue) {
    CollectionUtil.setLastElement(values, newValue);
  }

  /**
   * Useful for manipulating values after they have been recorded.
   */
  public List<Object> getRecordedValues() {
    return values;
  }
}
