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

import javax.sql.DataSource

import org.unitils.dbunit.annotation.DataSet
import org.unitils.database.annotations.TestDataSource

import groovy.sql.Sql

import spock.lang.*
import spock.unitils.UnitilsSupport

import static org.unitils.reflectionassert.ReflectionAssert.assertPropertyLenientEquals

@UnitilsSupport
@DataSet
class UserDaoSpec extends Specification {
  @Shared Sql sql = Sql.newInstance("jdbc:h2:mem:userdb", "org.h2.Driver")

  def setupSpec() {
    sql.execute("create table `user` (first_name varchar(25), last_name varchar(50), age int)")
  }

  def cleanupSpec() {
    sql.close()
  }

  @TestDataSource
  private DataSource dataSource

  def "find user by name"() {
    UserDao userDao = new UserDao(dataSource)
    def user = userDao.findByName("john", "doe")

    expect:
    assertPropertyLenientEquals("firstName", "john", user)
  }

  def "find users by minimal age"() {
    UserDao userDao = new UserDao(dataSource)
    List<User> users = userDao.findByMinimalAge(18)

    expect:
    assertPropertyLenientEquals("age", [39], users)
  }
}

