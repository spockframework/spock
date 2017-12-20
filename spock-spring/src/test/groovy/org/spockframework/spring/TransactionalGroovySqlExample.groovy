/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.spring

import spock.lang.*

import javax.sql.DataSource

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.BeforeTransaction
import org.springframework.transaction.annotation.Transactional

/*
 * Shows how to leverage the Spring TestContext framework's transaction
 * support, using groovy.sql.Sql for interacting with the database.
 *
 * To learn more about transaction management in the Spring TestContext
 * framework, see:
 * http://static.springsource.org/spring/docs/3.0.x/reference/testing.html#testcontext-tx
 *
 * Note: Due to the way Spring's TestContext framework is designed,
 * @Shared fields cannot be injected. This also means that
 * setupSpec() and cleanupSpec() cannot get access to Spring beans.
 */
@ContextConfiguration(locations = "TransactionalGroovySqlExample-context.xml")
class TransactionalGroovySqlExample extends Specification {
  @Shared
  boolean tableCreated = false

  @Shared
  Sql sql

  @Autowired
  DataSource dataSource

  /*
   * Populates the database. To prevent automatic rollback, we use a
   * @BeforeTransaction rather than a setup() method. Checking the
   * tableCreated flag ensures that the code is only run once.
   */
  @BeforeTransaction
  def createTable() {
    if (tableCreated) return
    sql = new Sql(dataSource)
    sql.execute("create table data (id int, name varchar(25))")
    sql.executeUpdate("insert into data (id, name) values (?, ?), (?, ?)", [1, "fred", 2, "tom"])
    tableCreated = true
  }

  @Transactional
  def "delete rows from table"() {
    setup:
    sql.execute("delete from data")
  }

  @Transactional
  def "table is back to initial state"() {
    expect:
    sql.rows("select * from data order by id") == [[ID: 1, NAME: "fred"], [ID: 2, NAME: "tom"]]
  }
}
