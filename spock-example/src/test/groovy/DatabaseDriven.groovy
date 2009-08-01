import org.junit.runner.RunWith
import spock.lang.*
import static spock.lang.Predef.*

import groovy.sql.Sql

@Speck
@RunWith(Sputnik)
class DatabaseDriven {
  @Shared sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")
  
  // usually, the test data would already be contained in the database
  def setupSpeck() {
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