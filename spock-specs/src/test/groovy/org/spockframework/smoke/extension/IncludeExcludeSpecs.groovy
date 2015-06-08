/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

class IncludeExcludeSpecs extends EmbeddedSpecification {
  List specs

  def setup() {
    compiler.addPackageImport(getClass().package)

    specs = compiler.compileWithImports("""
@Slow
class Spec1 extends Specification {
  def feature() {
    expect: true
  }
}

@Fast
class Spec2 extends Specification {
  def feature() {
    expect: true
  }
}

class Spec3 extends Specification {
  def feature() {
    expect: true
  }
}
  """)
  }

  def "include specs based on annotations or patterns"() {
    runner.configurationScript = {
      runner {
        include(*criteria)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 3 - runCount // cannot prevent JUnit from running excluded specs, so they get ignored

    where:
    criteria            || runCount
    [Slow]              || 1
    [Fast]              || 1
    [Slow, Fast]        || 2

    ['Slow']            || 1
    ['Fast']            || 1
    ['Slow', 'Fast']    || 2

    ['Spec1']           || 1
    ['Spec[12]']        || 2
    ['Spec.']           || 3
  }

  def "exclude specs based on annotations or patterns"() {
    runner.configurationScript = {
      runner {
        exclude(*criteria)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 3 - runCount // cannot prevent JUnit from running excluded specs, so they get ignored

    where:
    criteria            || runCount
    [Slow]              || 2
    [Fast]              || 2
    [Slow, Fast]        || 1

    ['Slow']            || 2
    ['Fast']            || 2
    ['Slow', 'Fast']    || 1

    ['Spec1']           || 2
    ['Spec[12]']        || 1
    ['Spec.']           || 0
  }

  def "include and exclude specs based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annTypes1)
        exclude(*annTypes2)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 3 - runCount // cannot prevent JUnit from running excluded specs, so they get ignored
    
    where:
    annTypes1   << [[Slow], [Slow], [Slow],       [Fast], [Fast], [Fast],       [Slow, Fast], [Slow, Fast], [Slow, Fast]]
    annTypes2   << [[Slow], [Fast], [Slow, Fast], [Slow], [Fast], [Slow, Fast], [Slow],       [Fast],       [Slow, Fast]]
    runCount    << [0,      1,      0,            1,      0,      0,            1,            1,            0           ]
  }

  def "include and exclude specs based on patterns"() {
    runner.configurationScript = {
      runner {
        include(*includes)
        exclude(*excludes)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 3 - runCount // cannot prevent JUnit from running excluded specs, so they get ignored

    where:
    includes            | excludes          || runCount
    ['Slow']            | ['Slow']          || 0
    ['Slow']            | ['Fast']          || 1
    ['Slow']            | ['Slow', 'Fast']  || 0
    ['Fast']            | ['Slow']          || 1
    ['Fast']            | ['Fast']          || 0
    ['Fast']            | ['Slow', 'Fast']  || 0
    ['Slow', 'Fast']    | ['Slow']          || 1
    ['Slow', 'Fast']    | ['Fast']          || 1
    ['Slow', 'Fast']    | ['Slow', 'Fast']  || 0
  }
}
