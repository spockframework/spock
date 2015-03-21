/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SafeIterationNameProvider implements NameProvider<IterationInfo> {
  private final NameProvider<IterationInfo> delegate;
  private final AtomicInteger iterationCount = new AtomicInteger(0);
  private final ConcurrentMap<String, Boolean> usedNames = new ConcurrentHashMap<String, Boolean>();

  public SafeIterationNameProvider(NameProvider<IterationInfo> delegate) {
    this.delegate = delegate;
  }

  public String getName(IterationInfo iteration) {
    final String name = getNameImpl(iteration);
    if (iteration.getParent().isReportIterations()){
      return unique(name);
    }else {
      return name;
    }
  }

    private String unique(String name) {
        if (tryRegisterName(name)){
            return name;
        }else {
            for (int i=1; i<Integer.MAX_VALUE; i++){
                final String newName = name + " [" + i + "]";
                if (tryRegisterName(newName)){
                    return newName;
                }
            }
        }
        throw new RuntimeException("will not reach here");
    }

    private boolean tryRegisterName(String name) {
        return usedNames.putIfAbsent(name, Boolean.TRUE) == null;
    }

    private String getNameImpl(IterationInfo iteration) {
    String safeName = iteration.getParent().isReportIterations() ?
        String.format("%s[%d]", iteration.getParent().getName(), iterationCount.getAndIncrement()) : iteration.getParent().getName();

    if (delegate == null) return safeName;

    try {
      String name = delegate.getName(iteration);
      if (name != null) return name;
      return safeName;
    } catch (Exception e) {
      return safeName;
    }
  }
}
