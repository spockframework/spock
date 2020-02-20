package org.spockframework.docs.datadriven

import groovy.sql.Sql
import groovy.transform.ToString
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DataSpec extends Specification {

// tag::datasource[]
  @Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")
// end::datasource[]

  def setupSpec() {
    sql.execute 'drop table if exists maxdata'
    sql.execute '''
    create table maxdata(
        a integer not null,
        b integer not null,
        x integer not null,
        c integer not null
    )
    '''

    sql.execute 'insert into maxdata values(1, 3, 10, 3)'
    sql.execute 'insert into maxdata values(7, 4, 10, 7)'
    sql.execute 'insert into maxdata values(0, 0, 10, 0)'
  }

  def cleanupSpec() {
    sql.execute 'drop table if exists maxdata'
  }

  def "single column"() {
    expect:
    a >= 0

// tag::single-column[]
    where:
    a | _
    1 | _
    7 | _
    0 | _
// end::single-column[]
  }

// tag::sql-data-pipe[]
  def "maximum of two numbers"() {
    expect:
    Math.max(a, b) == c

    where:
    [a, b, c] << sql.rows("select a, b, c from maxdata")
  }
// end::sql-data-pipe[]

  def "maximum of two numbers star"() {
    expect:
    Math.max(a, b) == c

// tag::sql-data-pipe-with-underscore[]
    where:
    [a, b, _, c] << sql.rows("select * from maxdata")
// end::sql-data-pipe-with-underscore[]
  }

  def "maximum of two numbers data variable assignment"() {
    expect:
    Math.max(a, b) == c

// tag::data-variable-assignment[]
    where:
    a = 3
    b = Math.random() * 100
    c = a > b ? a : b
// end::data-variable-assignment[]
  }

  def "maximum of two numbers sql data variable assignment"() {
    expect:
    Math.max(a, b) == c

// tag::sql-data-variable-assignment[]
    where:

    where:
    row << sql.rows("select * from maxdata")
    // pick apart columns
    a = row.a
    b = row.b
    c = row.c
// end::sql-data-variable-assignment[]
  }

  def "maximum of two numbers combined assignment"() {
    expect:
    Math.max(a, b) == c

// tag::combined-variable-assignment[]
    where:
    a | _
    1 | _
    7 | _
    0 | _

    b << [3, 4, 0]

    c = a > b ? a : b
// end::combined-variable-assignment[]
  }

  def "data pipes"() {
    expect:
    Math.max(a, b) == c

// tag::data-pipes[]
    where:
    a << [1, 7, 0]
    b << [3, 4, 0]
    c << [3, 7, 0]
// end::data-pipes[]
  }

  @Unroll
// tag::unrolled-1[]
  def "#person is #person.age years old"() { // property access
// end::unrolled-1[]
    expect:
    person

    where:
    person << [new Person(age: 14, name: 'Phil Cole')]
  }

  @Unroll
// tag::unrolled-2[]
  def "#person.name.toUpperCase()"() { // zero-arg method call
// end::unrolled-2[]
    expect:
    person

    where:
    person << [new Person(age: 14, name: 'Phil Cole')]
  }


  @Unroll
// tag::unrolled-3a[]
  def "#lastName"() { // zero-arg method call
// end::unrolled-3a[]
    expect:
    person

// tag::unrolled-3b[]
    where:
    person << [new Person(age: 14, name: 'Phil Cole')]
    lastName = person.name.split(' ')[1]
  }
// end::unrolled-3b[]

  @ToString(includes = ['name'], includePackage = false)
  static class Person {
    int age
    String name
  }
}

