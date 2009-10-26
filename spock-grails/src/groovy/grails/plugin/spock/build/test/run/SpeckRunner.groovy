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

package grails.plugin.spock.build.test.run

import grails.plugin.spock.build.test.adapter.ResultAdapter
import org.junit.runner.JUnitCore

class SpeckRunner {
  protected final listener
  protected final junit

  SpeckRunner(File reportsDir, List<String> formats) {
    listener = new SpeckRunListener(reportsDir, formats, System.out)
    junit = new JUnitCore()
    junit.addListener(listener)
  }

  def runTests(suite) {
    new ResultAdapter(junit.run(suite.specks as Class[]))
  }
}