/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.spring.mock.imported

import org.spockframework.spring.SpringBean
import spock.lang.Specification

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.*
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.interceptor.*
import org.springframework.context.annotation.*
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration

/**
 * Test {@link SpringBean} when mixed with Spring AOP.
 *
 * original author Phillip Webb
 * @author Leonard Brünings
 * @see <a href="https://github.com/spring-projects/spring-boot/issues/5837">5837</a>
 */
@ContextConfiguration
class SpringBeanWithAopProxySpec extends Specification {

  @SpringBean
  DateService dateService = Mock()

  def 'verify use proxy target'() throws Exception {
    when:
    Long d1 = dateService.getDate(false)

    then:
    d1 == 1L
    1 * dateService.getDate(false) >> 1L

    when:
    Long d2 = dateService.getDate(false)

    then:
    d2 == 2L
    1 * dateService.getDate(false) >> 2L
  }

  @Configuration
  @EnableCaching(proxyTargetClass = true)
  @Import(DateService.class)
  static class Config {

    @Bean
    CacheResolver cacheResolver(CacheManager cacheManager) {
      return new SimpleCacheResolver(cacheManager: cacheManager)
    }

    @Bean
    ConcurrentMapCacheManager cacheManager() {
      return new ConcurrentMapCacheManager(cacheNames: ['test'])
    }

  }

  @Service
  static class DateService {

    @Cacheable(cacheNames = "test")
    Long getDate(boolean argument) {
      return System.nanoTime()
    }

  }

}
