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
 *
 * @author Peter Niederwieser
 */
public class TransformErrorMessage extends Message {
  private final SourceUnit sourceUnit;
  private final Throwable cause;
  private final boolean printStackTrace;

  public TransformErrorMessage(SourceUnit sourceUnit, Throwable cause,
                               boolean printStackTrace) {
    this.sourceUnit = sourceUnit;
    this.cause = cause;
    this.printStackTrace = printStackTrace;
  }

  public void write(PrintWriter writer, Janitor janitor) {
    writer.print(sourceUnit.getName());
    if (cause != null) {
      writer.println(cause.getMessage());
      if (printStackTrace)
        cause.printStackTrace(writer);
    }
  }

  public Throwable getCause() {
    return cause;
  }
}
