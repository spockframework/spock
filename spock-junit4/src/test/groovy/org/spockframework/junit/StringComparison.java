/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit;

import org.junit.ComparisonFailure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Conclusions:
 * 1. JUnit's string diffing is pretty simplistic (in the test below,
 * JUnit doesn't find any similarities between the two strings)
 * 2. As soon as at least one string contains a line break,
 * IDEA offers to compare strings in diff dialog.
 */
public class StringComparison {
  @Test(expected = ComparisonFailure.class)
  public void testCompareStrings() {
    assertEquals("1aaaaaaaa2", "2aaaaaaaa1");
  }
}
