/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class HtmlReportGeneratorSpec extends Specification {
  @Rule TemporaryFolder tempFolder
  def generator = new HtmlReportGenerator()

  def setup() {
    generator.outputDirectory = tempFolder.root
    generator.reportName = "Test Report"
    generator.reportFileName = "report.html"
  }

  def "generate resources"() {
    //when:
    generator.generate()

    expect:
    tempFolder.root.list() as Set == ["img", "css", "js", "report.html"] as Set
    new File(tempFolder.root, "img").list().size() > 0
    new File(tempFolder.root, "css").list().size() > 0
    new File(tempFolder.root, "js").list().size() > 0
  }
}
