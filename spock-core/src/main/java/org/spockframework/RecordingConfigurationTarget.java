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

package org.spockframework;

import org.spockframework.builder.IConfigurationSource;
import org.spockframework.builder.IConfigurationTarget;

import java.util.ArrayList;
import java.util.List;

public class RecordingConfigurationTarget implements IConfigurationTarget {
  List<ISlotOperation> operations = new ArrayList<ISlotOperation>();
  
  public Object getSubject() {
    throw new UnsupportedOperationException("getSubject");
  }

  public IConfigurationTarget readSlot(String name) {
    throw new UnsupportedOperationException("readSlot");
  }

  public void writeSlot(String name, Object value) {
    operations.add(new WriteSlotOperation(name, value));
  }

  public void configureSlot(String name, List<Object> values, IConfigurationSource source) {
    operations.add(new ConfigureSlotOperation(name, values, source));
  }
  
  public void playback(IConfigurationTarget target) {
    for (ISlotOperation operation: operations) {
      operation.playback(target);  
    }
  }
  
  private interface ISlotOperation {
    void playback(IConfigurationTarget target);
  }
  
  private static class WriteSlotOperation implements ISlotOperation {
    final String name;
    final Object value;

    private WriteSlotOperation(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public void playback(IConfigurationTarget target) {
      target.writeSlot(name, value);
    }
  }
  
  private static class ConfigureSlotOperation implements ISlotOperation {
    final String name;
    final List<Object> values;
    final IConfigurationSource source;

    private ConfigureSlotOperation(String name, List<Object> values, IConfigurationSource source) {
      this.name = name;
      this.values = values;
      this.source = source;
    }

    public void playback(IConfigurationTarget target) {
      target.configureSlot(name, values, source);
    }
  }
}
