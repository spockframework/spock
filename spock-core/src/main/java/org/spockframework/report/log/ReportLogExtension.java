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

package org.spockframework.report.log;

import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.IoUtil;

import java.io.File;

public class ReportLogExtension extends AbstractGlobalExtension {
  private final StandardStreamsCapturer streamsCapturer = new StandardStreamsCapturer();
  private AsyncStandardStreamsListener logWriterListener;
  private AsyncStandardStreamsListener logClientListener;

  private ReportLogWriter logWriter;
  private ReportLogClient logClient;

  volatile ReportLogConfiguration reportConfig;

  @Override
  public void visitSpec(SpecInfo spec) {
    if (!reportConfig.enabled) return;

    if (logWriterListener != null) {
      spec.addListener(logWriterListener);
    }
    if (logClientListener != null) {
      spec.addListener(logClientListener);
    }
    if (logWriterListener != null || logClientListener != null) {
      spec.addListener(new AbstractRunListener() {
        @Override
        public void beforeSpec(SpecInfo theSpec) {
          streamsCapturer.start();
        }
      });
    }
  }

  @Override
  public void start() {
    if (!reportConfig.enabled) return;

    File logFile = reportConfig.getLogFile();
    if (logFile != null) {
      logWriter = new ReportLogWriter(logFile);
      logWriter.setPrefix("loadLogFile(");
      logWriter.setPostfix(")\n\n");
      logWriter.start();
      logWriterListener = createRunListener("spock-report-log-writer", logWriter);
      logWriterListener.start();
    }

    if (reportConfig.reportServerAddress != null) {
      logClient = new ReportLogClient(reportConfig.reportServerAddress, reportConfig.reportServerPort);
      logClient.start();
      logClientListener = createRunListener("spock-report-log-client", logClient);
      logClientListener.start();
    }
  }

  @Override
  public void stop() {
    if (!reportConfig.enabled) return;
    IoUtil.stopQuietly(streamsCapturer, logWriterListener, logWriter, logClientListener, logClient);
  }

  private AsyncStandardStreamsListener createRunListener(String name, IReportLogListener logListener) {
    ReportLogEmitter emitter = new ReportLogEmitter();
    emitter.addListener(logListener);
    AsyncStandardStreamsListener listener = new AsyncStandardStreamsListener(name, emitter, emitter);
    streamsCapturer.addStandardStreamsListener(listener);
    return listener;
  }
}
