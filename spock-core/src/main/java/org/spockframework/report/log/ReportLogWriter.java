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

package org.spockframework.report.log;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.util.*;

import java.io.*;
import java.util.*;

public class ReportLogWriter implements IReportLogListener, IStoppable {
  private final File logFile;

  private String prefix = "";
  private String postfix = "";
  private boolean prettyPrint = true;
  private boolean liveUpdate = true;

  private final ReportLogMerger logMerger = new ReportLogMerger();
  private final Map<String, Map<String, Object>> pendingLogs = new HashMap<>();
  private Writer fileWriter;
  private JsonWriter jsonWriter;

  public ReportLogWriter(File logFile) {
    this.logFile = logFile;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setPostfix(String postfix) {
    this.postfix = postfix;
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public void setLiveUpdate(boolean liveUpdate) {
    this.liveUpdate = liveUpdate;
  }

  public void start() {
    logFile.getParentFile().mkdirs();
    try {
      logFile.createNewFile();
      fileWriter = new OutputStreamWriter(new FileOutputStream(logFile), "utf-8");
    } catch (IOException e) {
      throw new ExtensionException("Error creating report log file: " + logFile, e);
    }
    jsonWriter = new JsonWriter(fileWriter);
    jsonWriter.setPrettyPrint(prettyPrint);
  }

  @Override
  public void stop() {
    try {
      for (Map spec : pendingLogs.values()) {
        writeLog(spec);
      }
    } finally {
      IoUtil.closeQuietly(fileWriter);
    }
  }

  @Override
  public void emitted(Map<String, Object> log) {
    String key = log.get("package") + "." + log.get("name");
    Map<String, Object> mergedLog = logMerger.merge(pendingLogs.get(key), log);
    if (mergedLog.get("result") != null) {
      writeLog(mergedLog);
      pendingLogs.remove(key);
    } else {
      pendingLogs.put(key, mergedLog);
    }
  }

  private void writeLog(Map log) {
    try {
      fileWriter.write(prefix);
      fileWriter.write("[");
      jsonWriter.write(log);
      fileWriter.write("]");
      fileWriter.write(postfix);
      if (liveUpdate) {
        fileWriter.flush();
      }
    } catch (IOException e) {
      throw new ExtensionException("Error writing to report log file: " + logFile, e);
    }
  }
}
