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

import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class DatabaseDriven extends Specification {
  @Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")
  
  // insert data (usually the database would already contain the data)
  def setupSpec() {
    sql.execute("create table maxdata (id int primary key, a int, b int, c int)")
    sql.execute("insert into maxdata values (1, 3, 7, 7), (2, 5, 4, 5), (3, 9, 9, 9)")
  }

  def "maximum of two numbers"() {
    expect:
    Math.max(a, b) == c

    where:
    [a, b, c] << sql.rows("select a, b, c from maxdata")
  }
}