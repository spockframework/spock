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

/**
 * Records the values in a condition.
 *
 * @author Peter Niederwieser
 */
public class ValueRecorder {
  private final ArrayList<Object> values = new ArrayList<Object>();
  private final Deque<Integer> startedRecordings = new ArrayDeque<Integer>();

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

    boolean foundThisCallOnStack = false;
    while (!startedRecordings.isEmpty()){
      final Integer indexFromStack = startedRecordings.pop();
      if (indexFromStack==index){
        foundThisCallOnStack = true;
        break;
      }
    }
    if (!foundThisCallOnStack){
      throw new IllegalStateException("can not found call #"+index+" on stack. Invalid call of record method?");
    }

    return value;
  }

  public static final String START_RECORDING_VALUE = "startRecordingValue";

  public int startRecordingValue(int index){
    startedRecordings.push(index);
    return index;
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

  public List<Object> getValues() {
    return new ArrayList<Object>(values);
  }

  public Integer getCurrentRecordingVarNum() {
    if (startedRecordings.isEmpty()) {
      return null;
    } else {
      return startedRecordings.peek();
    }
  }
}
