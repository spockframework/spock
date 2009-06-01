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

package org.spockframework.idea;

import org.junit.*;

/**
 * See http://www.jetbrains.net/jira/browse/IDEADEV-36531.
 * 
 * @author Peter Niederwieser
 */
public class IdeaGarblesAssertionMessage {
  @Test
  @Ignore
  public void test() {
    throw new AssertionError("\n1\n2\n3\n[junit.textui.TestRunner.doRun(TestRunner.java:109)]"); // same for any other exception type
  }
}
