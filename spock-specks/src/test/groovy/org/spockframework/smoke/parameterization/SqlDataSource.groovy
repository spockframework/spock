package org.spockframework.smoke.parameterization

import org.junit.runner.RunWith
import spock.lang.*
import static spock.lang.Predef.*

import groovy.sql.Sql

@Speck
@RunWith(Sputnik)
class SqlDataSource {
   @Shared Sql sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

  def setupSpeck() {
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