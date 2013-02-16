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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.spockframework.util.IoUtil;
import org.spockframework.util.Nullable;

import spock.config.ConfigurationObject;

@ConfigurationObject("report")
public class ReportLogConfiguration {
  public boolean enabled = false;

  public String logFileDir = System.getProperty("spock.logFileDir");
  public String logFileName = System.getProperty("spock.logFileName");
  public String logFileSuffix = System.getProperty("spock.logFileSuffix");

  public String issueNamePrefix = "";
  public String issueUrlPrefix = "";

  public String reportServerAddress = System.getProperty("spock.reportServerAddress");
  public int reportServerPort = Integer.valueOf(System.getProperty("spock.reportServerPort", "4242"));

  public String getLogFileSuffix() {
    if (logFileSuffix != null && logFileSuffix.contains("#timestamp")) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String timestamp = dateFormat.format(new Date());
      logFileSuffix = logFileSuffix.replace("#timestamp", timestamp);
    }
    return logFileSuffix;
  }

  @Nullable
  public File getLogFile() {
    if (logFileDir == null) return null;

    String fullName = logFileName;
    String suffix = getLogFileSuffix();
    if (suffix != null) {
      String extension = IoUtil.getFileExtension(logFileName);
      if (extension == null) {
        fullName = logFileName + "-" + suffix;
      }  else {
        fullName = logFileName.substring(0, logFileName.length() - extension.length() - 1)
            + "-" + suffix + "." + extension;
      }
    }
    return new File(logFileDir, fullName);
  }
}
