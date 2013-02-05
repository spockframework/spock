/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report.log

import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
Look Ma'! Now specs
can have really cool
narratives!
""")
class ReportLogMergerSpec extends Specification {
  def merger = new ReportLogMerger()

  def "merge maps"() {
    def map1 = [key1: "value 1", key2: "value 2", key3: "value 3"]
    def map2 = [key3: "replaced", key4: "value 4", key5: "value 5"]

    expect:
    merger.merge(map1, map2) == [key1: "value 1", key2: "value 2", key3: "replaced", key4: "value 4", key5: "value 5"]
  }

  def "merge lists"() {
    def list1 = ["item1", "item2", "item3"]
    def list2 = ["item4", "item5", "item6"]

    expect:
    merger.merge(list1, list2) == ["item1", "item2", "item3", "item4", "item5", "item6"]
  }

  def "merge name-indexed lists"() {
    def list1 = [
        [name: "key1", age: 11, size: 111],
        [name: "key2", age: 22, size: 222],
        [name: "key3", age: 33, size: 333]
    ]

    def list2 = [
        [name: "key3", size: "supersizeme", pet: "dog3"],
        [name: "key4", size: 444, pet: "dog4"],
        [name: "key5", size: 555, pet: "dog5"]
    ]

    def merged = [
        [name: "key1", age: 11, size: 111],
        [name: "key2", age: 22, size: 222],
        [name: "key3", age: 33, size: "supersizeme", pet: "dog3"],
        [name: "key4", size: 444, pet: "dog4"],
        [name: "key5", size: 555, pet: "dog5"]
    ]

    expect: merger.merge(list1, list2) == merged
  }

  def "merge values"() {
    expect:
    merger.merge(0, 3) == 3
    merger.merge(3, 0) == 0
    merger.merge("foo", "bar") == "bar"
    merger.merge("bar", "foo") == "foo"
  }

  def "merge recursively"() {
    def map1 = [
        map: [key1: "key 1", key2: "key 2"],
        list: [ "item1", "item2"],
        nameIndexedList: [
            [name: "name1", size: "small"],
            [name: "name2", size: "large"]
        ],
        value1: "value 1",
        value2: "value 2"
    ]

    def map2 = [
        map: [key3: "key 3", key2: "key changed"],
        list: [ "item2", "item3"],
        nameIndexedList: [
            [name: "name3", size: "medium"],
            [name: "name2", size: "changed"]
        ],
        value3: "value 3",
        value2: "changed"
    ]

    def merged = [
        map: [key1: "key 1", key2: "key changed", key3: "key 3"],
        list: [ "item1", "item2", "item2", "item3"],
        nameIndexedList: [
            [name: "name1", size: "small"],
            [name: "name2", size: "changed"],
            [name: "name3", size: "medium"],
        ],
        value1: "value 1",
        value2: "changed",
        value3: "value 3"
    ]

    expect:
    merger.merge(map1, map2) == merged
  }
}
