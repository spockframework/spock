package org.spockframework.docs.datadriven

import groovy.sql.Sql
import groovy.transform.ToString
import spock.lang.Shared
import spock.lang.Specification

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

  def "multiple tables"() {
    expect:
    a >= 0
    b < c

// tag::multiple-tables[]
    where:
    a | _
    1 | _
    7 | _
    0 | _
    __

    b | c
    1 | 2
    3 | 4
    5 | 6
// end::multiple-tables[]
  }

  def "multiple tables combined"() {
    expect:
    a >= 0
    b < c

// tag::multiple-tables-combined[]
    where:
    a | b | c
    1 | 1 | 2
    7 | 3 | 4
    0 | 5 | 6
// end::multiple-tables-combined[]
  }

  def "multiple tables with top border"() {
    expect:
    a >= 0
    b < c

// tag::multiple-tables-with-top-border[]
    where:
    _____
    a | _
    1 | _
    7 | _
    0 | _
    _____
    b | c
    1 | 2
    3 | 4
    5 | 6
// end::multiple-tables-with-top-border[]
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

  def "nested multi-variable data pipes"() {
    expect:
    a in [['a1', 'a2'], ['a2', 'a1']]
    [b, c] in [['b1', 'c1'], ['b2', 'c2']]

// tag::nested-multi-variable-data-pipe[]
    where:
    [a, [b, _, c]] << [
      ['a1', 'a2'].permutations(),
      [
        ['b1', 'd1', 'c1'],
        ['b2', 'd2', 'c2']
      ]
    ].combinations()
// end::nested-multi-variable-data-pipe[]
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
    row << sql.rows("select * from maxdata")
    // pick apart columns
    a = row.a
    b = row.b
    c = row.c
// end::sql-data-variable-assignment[]
  }

  def "maximum of two numbers previous column access"() {
    expect:
    Math.max(a, b) == b

// tag::previous-column-access[]
    where:
    a | b
    3 | a + 1
    7 | a + 2
    0 | a + 3
// end::previous-column-access[]
  }

  def "maximum of two numbers previous column access over multiple tables"() {
    expect:
    Math.max(a, b) == b
    Math.max(d, e) == e

// tag::previous-column-access-multi-table[]
    where:
    a | b
    3 | a + 1
    7 | a + 2
    0 | a + 3

    and:
    c = 1

    and:
    d     | e
    a * 2 | b * 2
    a * 3 | b * 3
    a * 4 | b * 4
// end::previous-column-access-multi-table[]
  }

// tag::sql-multi-assignment[]
  def "maximum of two numbers multi-assignment"() {
    expect:
    Math.max(a, b) == c

    where:
    row << sql.rows("select a, b, c from maxdata")
    (a, b, c) = row
  }
// end::sql-multi-assignment[]

  def "maximum of two numbers multi-assignment star"() {
    expect:
    Math.max(a, b) == c

// tag::sql-multi-assignment-with-underscore[]
    where:
    row << sql.rows("select * from maxdata")
    (a, b, _, c) = row
// end::sql-multi-assignment-with-underscore[]
  }

  def "maximum of two numbers combined assignment"() {
    expect:
    Math.max(a, c) == d

// tag::combined-variable-assignment[]
    where:
    a | b
    1 | a + 1
    7 | a + 2
    0 | a + 3

    c << [3, 4, 0]

    d = a > c ? a : c
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

// tag::unrolled-1[]
  def "#person is #person.age years old"() { // property access
// end::unrolled-1[]
    expect:
    person

    where:
    person << [new Person(age: 14, name: 'Phil Cole')]
  }

// tag::unrolled-2[]
  def "#person.name.toUpperCase()"() { // zero-arg method call
// end::unrolled-2[]
    expect:
    person

    where:
    person << [new Person(age: 14, name: 'Phil Cole')]
  }


// tag::unrolled-3a[]
  def "#lastName"() {
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

