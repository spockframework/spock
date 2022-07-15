/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Dummy Groovy 3 class implementation to keep codebase compilable also with Groovy 2.5
 */
public class LambdaExpression extends ClosureExpression {

  public LambdaExpression(Parameter[] parameters, Statement code) {
    super(parameters, code);
    throw new UnsupportedOperationException("Dummy LambdaExpression providing Spock codebase compatibility with both Groovy 2 and 3 " +
      "should not be instantiated. Make sure you do not mix spock-2.x-groovy-2.5 and groovy-3 artifacts on classpath.");
  }
}
