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

import org.spockframework.spring.*
import org.springframework.core.SpringVersion
import spock.lang.*

import javax.inject.Inject

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.*
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.interceptor.*
import org.springframework.context.annotation.*
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration

/**
 * Test {@link SpringSpy} when mixed with Spring AOP.
 *
 * original author Phillip Webb
 * @author Leonard Brünings
 * @see <a href="https://github.com/spring-projects/spring-boot/issues/5837">5837</a>
 */
@ContextConfiguration
class SpringSpyWithAopProxyTests extends Specification {

  @SpringSpy
  @UnwrapAopProxy
  private DateService spyService

  @Inject
  private DateService dateService

  @PendingFeatureIf(
    reason = 'Spring before 5.0.0.RELEASE has an incompatibility with ByteBuddy',
    value = {
      (SpringVersion.getVersion().split(/\./, 2).first().toInteger() < 5) &&
        !sys.'org.spockframework.mock.ignoreByteBuddy'?.toBoolean()
    })
  def 'verify use proxyTarget'() throws Exception {
    when:
    Long d1 = dateService.getDate(false)
    Thread.sleep(200)
    Long d2 = dateService.getDate(false)

    then:
    d1 == d2
    1 * spyService.getDate(false)
  }

  @Configuration
  @EnableCaching(proxyTargetClass = true)
  @Import(DateService)
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
    Long getDate(boolean arg) {
      return System.nanoTime()
    }

  }

}
