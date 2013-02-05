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

package org.spockframework.report.log

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import org.spockframework.util.GroovyUtil
import org.spockframework.util.JsonWriter
import org.spockframework.util.ThreadSafe

@ThreadSafe // `emitted()` may be called from different threads
class ReportLogWriter implements IReportLogListener {
  private static final Map STOP = [:]

  private final File logFile
  private final BlockingQueue<Map> logQueue = new LinkedBlockingQueue<Map>()
  private final ReportLogMerger logMerger = new ReportLogMerger()

  private String prefix = ""
  private String postfix = ""
  private boolean prettyPrint = true
  private boolean liveUpdate = true

  private Writer fileWriter
  private JsonWriter jsonWriter
  private Thread writerThread

  ReportLogWriter(File logFile, String prefix, String postfix, boolean prettyPrint) {
    this.logFile = logFile
    this.prefix = prefix
    this.postfix = postfix
    this.prettyPrint = prettyPrint
  }

  void start() {
    Map<String, Map> pendingLogs = [:]
    logFile.parentFile.mkdirs()
    logFile.createNewFile()
    fileWriter = new OutputStreamWriter(new FileOutputStream(logFile), "utf-8")
    jsonWriter = new JsonWriter(fileWriter)
    jsonWriter.prettyPrint = prettyPrint

    writerThread = Thread.start("spock-report-file-writer") {
      GroovyUtil.tryAll({
        def log = logQueue.take()
        while(!log.is(STOP)) {
          process(log, pendingLogs)
          log = logQueue.take()
        }
      }, {
        for (spec in pendingLogs.values()) {
          writeLog(spec)
        }
      }, {
        fileWriter.close()
      })
    }
  }

  void stop() {
    logQueue.put(STOP)
    writerThread.join()
  }

  void emitted(Map log) {
    logQueue.put(log)
  }

  private void process(Map log, Map<String, Map> pendingLogs) {
    def key = log.package + "." + log.name
    def mergedLog = logMerger.merge(pendingLogs[key], log)
    if (hasResult(mergedLog)) {
      writeLog(mergedLog)
      pendingLogs.remove(key)
    } else {
      pendingLogs[key] = mergedLog
    }
  }

  private boolean hasResult(Map log) {
    log.result
  }

  private void writeLog(Map log) {
    fileWriter.write(prefix)
    fileWriter.write("[")
    jsonWriter.write(log)
    fileWriter.write("]")
    fileWriter.write(postfix)
    if (liveUpdate) {
      fileWriter.flush()
    }
  }
}
