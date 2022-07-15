/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.unitils.dbunit

import groovy.sql.Sql
import javax.sql.DataSource

class UserDao {
  private Sql sql

  UserDao(DataSource dataSource) {
    sql = new Sql(dataSource)
  }

  User findByName(String firstName, String lastName) {
    def row = sql.firstRow("select * from user where first_name=? and last_name=?", [firstName, lastName])
    row2User(row)
  }

  List<User> findByMinimalAge(int age) {
    sql.rows("select * from user where age >= ?", [age]).collect {
      row2User(it)
    }
  }

  private row2User(row) {
    new User(firstName: row.first_name, lastName: row.last_name, age: row.age)
  }
}
