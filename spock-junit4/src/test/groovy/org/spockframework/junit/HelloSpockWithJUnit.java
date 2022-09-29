/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HelloSpockWithJUnit {
	private String name;
	private int length;

	public HelloSpockWithJUnit(String name, int length) {
		this.name = name;
		this.length = length;
	}

  @Parameters
	public static List<Object[]> data() {
		return Arrays.asList(new Object[][] {{"Spock", 5}, {"Kirk", 4}, {"Scotty", 6}});
	}

	@Test
	public void lengthOfSpockAndFriends() {
		assertEquals(length, name.length());
	}
}
