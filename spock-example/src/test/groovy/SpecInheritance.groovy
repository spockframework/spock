/*
 * Copyright 2009 the original author or authors.
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

import spock.lang.*

abstract class BaseSpec extends Specification {
  def x = { println 'base field initializer' }()

  def setupSpec() { println 'base setupSpec()' }
  def cleanupSpec() { println 'base cleanupSpec()' }

  def setup() { println 'base setup()' }
  def cleanup() { println 'base cleanup()' }

  def baseSpecMethod() { setup: println 'base spec method' }
}

class DerivedSpec extends BaseSpec {
  def y = { println 'derived field initializer' }()

  def setupSpec() { println 'derived setupSpec()' }
  def cleanupSpec() { println 'derived cleanupSpec()' }

  def setup() { println 'derived setup()' }
  def cleanup() { println 'derived cleanup()' }

  def derivedSpecMethod() { setup: println 'derived spec method' }
}