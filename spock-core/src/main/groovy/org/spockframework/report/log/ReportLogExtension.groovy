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

import org.spockframework.runtime.AsyncStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.GroovyUtil

class ReportLogExtension implements IGlobalExtension {
  private final StandardStreamsCapturer streamsCapturer = new StandardStreamsCapturer()
  private AsyncStandardStreamsListener logWriterListener;
  private AsyncStandardStreamsListener logClientListener;

  private boolean started = false
  private ReportLogWriter logWriter
  private ReportServerClient logClient

  volatile ReportLogConfiguration reportConfig

  void visitSpec(SpecInfo spec) {
    if (!reportConfig.enabled) return

    if (!started) {
      start()
      started = true
      Runtime.runtime.addShutdownHook(new Thread({ stop() }))
    }

    if (logWriterListener != null) {
      spec.addListener(logWriterListener)
      // make sure capturer is still installed
      streamsCapturer.start()
    }
    if (logClientListener != null) {
      spec.addListener(logClientListener)
      // make sure capturer is still installed
      streamsCapturer.start()
    }
  }

  void start() {
    def logFile = reportConfig.logFile
    if (logFile != null) {
      logWriter = new ReportLogWriter(logFile)
      logWriter.prefix = "loadLogFile("
      logWriter.postfix = ")\n\n"
      logWriter.start()
      logWriterListener = createRunListener("report-log-writer", logWriter)
      logWriterListener.start()
    }

    if (reportConfig.reportServerAddress != null) {
      logClient = new ReportServerClient(reportConfig.reportServerAddress, reportConfig.reportServerPort)
      logClient.start()
      logClientListener = createRunListener("report-log-client", logClient)
      logClientListener.start()
    }
  }

  void stop() {
    GroovyUtil.closeQuietly("stop", streamsCapturer, logWriterListener, logWriter, logClientListener, logClient)
  }

  private createRunListener(String name, IReportLogListener logListener) {
    def emitter = new ReportLogEmitter()
    emitter.addListener(logListener)
    def listener = new AsyncStandardStreamsListener(name, emitter, emitter)
    streamsCapturer.addStandardStreamsListener(listener)
    listener
  }
}
