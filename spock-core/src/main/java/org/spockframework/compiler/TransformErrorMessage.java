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

package org.spockframework.compiler;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public class TransformErrorMessage extends Message {
  private final SourceUnit sourceUnit;
  private final Throwable throwable;
  private final boolean printStackTrace;

  public TransformErrorMessage(SourceUnit sourceUnit, Throwable throwable,
                               boolean printStackTrace) {
    this.sourceUnit = sourceUnit;
    this.throwable = throwable;
    this.printStackTrace = printStackTrace;
  }

  public void write(PrintWriter writer, Janitor janitor) {
    writer.print(sourceUnit.getName());
    if (throwable != null) {
      writer.println(throwable.getMessage());
      if (printStackTrace)
        throwable.printStackTrace(writer);
    }
  }
}
