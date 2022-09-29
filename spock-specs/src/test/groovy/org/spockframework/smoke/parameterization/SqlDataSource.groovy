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

package org.spockframework.smoke.parameterization

import spock.lang.*

import groovy.sql.Sql

class SqlDataSource extends Specification {
  @Shared
  Sql sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

  def setupSpec() {
    sql.execute("create table data (id int primary key, a int, c int, b int)") // intentionally use "strange" order
    sql.execute("insert into data values (1, 3, 7, 7), (2, 5, 5, 4), (3, 9, 9, 9)")
  }

  def "simple parameterization"() {
    expect:
    Math.max(a, b) == c

    where:
    row << sql.rows("select * from data")
    a = row["a"]
    b = row["b"]
    c = row["c"]
  }

  def "multi-parameterization"() {
    expect:
    Math.max(a, b) == c

    where:
    [a, b, c] << sql.rows("select a, b, c from data")
  }

  def "multi-parameterization with wildcard"() {
    expect:
    Math.max(a, b) == c

    where:
    [_, a, c, b] << sql.rows("select * from data")
  }
}
