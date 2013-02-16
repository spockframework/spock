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

import org.spockframework.util.IoUtil
import org.spockframework.util.JsonWriter

class ReportLogWriter implements IReportLogListener {
  private final File logFile

  String prefix = ""
  String postfix = ""
  boolean prettyPrint = true
  boolean liveUpdate = true

  private final ReportLogMerger logMerger = new ReportLogMerger()
  private final Map<String, Map> pendingLogs = [:]
  private Writer fileWriter
  private JsonWriter jsonWriter

  ReportLogWriter(File logFile) {
    this.logFile = logFile
  }

  void start() {
    logFile.parentFile.mkdirs()
    logFile.createNewFile()
    fileWriter = new OutputStreamWriter(new FileOutputStream(logFile), "utf-8")
    jsonWriter = new JsonWriter(fileWriter)
    jsonWriter.prettyPrint = prettyPrint
  }

  void stop() {
    try {
      for (spec in pendingLogs.values()) {
        writeLog(spec)
      }
    } finally {
      IoUtil.closeQuietly(fileWriter)
    }
  }

  void emitted(Map log) {
    def key = log.package + "." + log.name
    def mergedLog = logMerger.merge(pendingLogs[key], log)
    if (mergedLog.result != null) {
      writeLog(mergedLog)
      pendingLogs.remove(key)
    } else {
      pendingLogs[key] = mergedLog
    }
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
