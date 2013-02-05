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

package org.spockframework.report.log

import org.spockframework.runtime.RunContext
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

class ReportLogExtension implements IGlobalExtension {
  ReportLogConfiguration reportConfig

  private boolean started

  private ReportLogWriter logWriter

  void visitSpec(SpecInfo spec) {
    if (!reportConfig.enabled) return

    if (!started) {
      start()
      started = true
    }

    def eventEmitter = new ReportLogEmitter(logWriter)
    // reclaiming and releasing standard out/err capture before/after each spec is the safest choice
    spec.addInterceptor(new StandardStreamCapturingInterceptor(eventEmitter))
    spec.addListener(eventEmitter)
  }

  void start() {
    logWriter = new ReportLogWriter(getLogFile(), "loadLogFile(", ")\n\n", true)
    logWriter.start()
    Runtime.runtime.addShutdownHook(new Thread({ stop() }))
  }

  void stop() {
    logWriter.stop()
  }

  private File getLogFile() {
    def logFileName = reportConfig.logFileName
    def logFileSuffix = reportConfig.logFileSuffix
    if (logFileSuffix) logFileName += "-$logFileSuffix"
    def logFileDir = reportConfig.logFileDir
    if (!logFileDir.absolute) {
      logFileDir = new File(RunContext.get().getSpockUserHome(), logFileDir.path)
    }
    new File(logFileDir, logFileName)
  }
}
