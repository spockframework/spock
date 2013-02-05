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

import spock.config.ConfigurationObject;

@ConfigurationObject("report")
public class ReportLogConfiguration {
  public boolean enabled = false;

  public File logFileDir = new File(System.getProperty("spock.logFileDir", "spock/logFiles"));
  public String logFileName = System.getProperty("spock.logFileName", "spock-log");
  public String logFileSuffix;

  public String issueNamePrefix = "";
  public String issueUrlPrefix = "";

  public ReportLogConfiguration() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    logFileSuffix = System.getProperty("spock.logFileSuffix", dateFormat.format(new Date()));
  }
}
