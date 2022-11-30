/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.spockframework.boot3

import org.spockframework.boot3.jpa.*
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.*

/**
 * Integration tests for ensuring compatibility with Spring-Boot's {@link DataJpaTest} annotation.
 */
@DataJpaTest
class DataJpaTestIntegrationSpec extends Specification {

  @Autowired
  TestEntityManager entityManager

  @Autowired
  BookRepository bookRepository

  def "spring context loads for data jpa slice"() {
    given: "some existing books"
    entityManager.persist(new Book("Moby Dick"))
    entityManager.persist(new Book("Testing Spring with Spock"))

    expect: "the correct count is inside the repository"
    bookRepository.count() == 2L
  }


}
