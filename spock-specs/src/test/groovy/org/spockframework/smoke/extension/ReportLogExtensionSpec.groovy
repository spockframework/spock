/*
 * Copyright 2013 the original author or authors.
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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.spockframework.EmbeddedSpecification

import java.util.regex.Matcher

class ReportLogExtensionSpec extends EmbeddedSpecification {
  @Rule TemporaryFolder tempDir
  def logFile

  def setup() {
    logFile = new File(tempDir.root, "report.json")
    runner.configurationScript = {
      report {
        enabled true
        logFileDir = logFile.parentFile
        logFileName = logFile.name
      }
    }
  }

  def "write execution log for spec with passing feature"() {
    when:
    runner.runWithImports(
"""
  @Narrative('''
As a user
I want foo
So that bar
''')
  @Title("A beautiful mind")
  @See("http://see.me")
  class MySpec extends Specification {
    @See("http://see.me.too")
    def "some feature"() {
      given: "foo"
      println("out1")

      when: "bar"
      System.err.println("err1")

      then: "baz"
      println("out2")

      and: "bazbaz"
      System.err.println("err2")
    }
  }
""")

    then:
    normalize(logFile.getText("utf-8")) == normalize(
"""
loadLogFile([{
    "package": "apackage",
    "name": "A beautiful mind",
    "start": 1360051647510,
    "narrative": "As a user\\nI want foo\\nSo that bar",
    "features": [
        {
            "name": "some feature",
            "start": 1360056471570,
            "narrative": "Given foo\\nWhen bar\\nThen baz\\nAnd bazbaz",
            "output": [
                "out1\\n",
                "out2\\n"
            ],
            "errorOutput": [
                "err1\\n",
                "err2\\n"
            ],
            "end": 1360051647586,
            "result": "passed",
            "attachments": [
                {
                    "name": "http:\\/\\/see.me.too",
                    "url": "http:\\/\\/see.me.too"
                }
            ]
        }
    ],
    "end": 1360051647593,
    "result": "passed",
    "attachments": [
        {
            "name": "http:\\/\\/see.me",
            "url": "http:\\/\\/see.me"
        }
    ]
}])
""")
  }

  def "write execution log for spec with failing feature"() {
    runner.throwFailure = false

    when:
    runner.runWithImports(
        """
  @Narrative('''
As a user
I want foo
So that bar
''')
  @Title("A beautiful mind")
  @See("http://see.me")
  class MySpec extends Specification {
    @See("http://see.me.too")
    def "some feature"() {
      given: "foo"
      println("out1")

      when: "bar"
      System.err.println("err1")

      then: "baz"
      println("out2")
      throwException()

      and: "bazbaz"
      System.err.println("err2")
    }

    private throwException() {
      def exception = new IOException("ouch")
      def elem = new StackTraceElement("DeclaringClass", "methodName", "filename", 42)
      exception.stackTrace = [elem]
      throw exception
    }
  }
""")

    then:
    normalize(logFile.getText("utf-8")) == normalize(
        """
loadLogFile([{
    "package": "apackage",
    "name": "A beautiful mind",
    "start": 1360051647510,
    "narrative": "As a user\\nI want foo\\nSo that bar",
    "features": [
        {
            "name": "some feature",
            "start": 1360056471570,
            "narrative": "Given foo\\nWhen bar\\nThen baz\\nAnd bazbaz",
            "output": [
                "out1\\n",
                "out2\\n"
            ],
            "errorOutput": [
                "err1\\n"
            ],
            "exceptions": [
                "Condition not satisfied:\\n\\nthrowException()\\n|\\njava.io.IOException: ouch\\n\\n\\tat apackage.MySpec.some feature(scriptXXXXXXXXXXXXXXXXXXXXXXX.groovy:XX)\\nCaused by: java.io.IOException: ouch\\n\\tat DeclaringClass.methodName(filename:XX)\\n"
            ],
            "end": 1360051647586,
            "result": "failed",
            "attachments": [
                {
                    "name": "http:\\/\\/see.me.too",
                    "url": "http:\\/\\/see.me.too"
                }
            ]
        }
    ],
    "end": 1360051647593,
    "result": "failed",
    "attachments": [
        {
            "name": "http:\\/\\/see.me",
            "url": "http:\\/\\/see.me"
        }
    ]
}])
""")
  }

  def "write execution log for spec with skipped feature"() {
    when:
    runner.runWithImports(
        """
  @Narrative('''
As a user
I want foo
So that bar
''')
  @Title("A beautiful mind")
  @See("http://see.me")
  class MySpec extends Specification {
    @See("http://see.me.too")
    @Ignore
    def "some feature"() {
      given: "foo"
      println("out1")

      when: "bar"
      System.err.println("err1")

      then: "baz"
      println("out2")

      and: "bazbaz"
      System.err.println("err2")
    }
  }
""")

    then:
    normalize(logFile.getText("utf-8")) == normalize(
        """
loadLogFile([{
    "package": "apackage",
    "name": "A beautiful mind",
    "start": 1360051647510,
    "narrative": "As a user\\nI want foo\\nSo that bar",
    "features": [
        {
            "name": "some feature",
            "start": 1360056471570,
            "end": 1360056471570,
            "result": "skipped",
            "narrative": "Given foo\\nWhen bar\\nThen baz\\nAnd bazbaz"
        }
    ],
    "end": 1360051647593,
    "result": "passed",
    "attachments": [
        {
            "name": "http:\\/\\/see.me",
            "url": "http:\\/\\/see.me"
        }
    ]
}])
""")
  }

  private String normalize(String str) {
    str.trim().replaceAll("\\d", "X").replaceAll("(\\\\r\\\\n|\\\\n)", Matcher.quoteReplacement("\\n"))
  }
}
