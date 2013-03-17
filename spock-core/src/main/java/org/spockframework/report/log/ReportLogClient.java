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

import java.io.*;
import java.net.Socket;
import java.util.Map;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.util.IStoppable;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.IoUtil;
import org.spockframework.util.JsonWriter;

public class ReportLogClient implements IReportLogListener, IStoppable {
  private final String reportServerAddress;
  private final int reportServerPort;

  private Socket socket;

  public ReportLogClient(String reportServerAddress, int reportServerPort) {
    this.reportServerAddress = reportServerAddress;
    this.reportServerPort = reportServerPort;
  }

  public void start() {
    try {
      socket = new Socket(reportServerAddress, reportServerPort);
    } catch (IOException e) {
      throw new ExtensionException(String.format("Error opening connection to report server. " +
          "Server address: %s Server port: %d", reportServerAddress, reportServerPort), e);
    }
  }

  public void stop() {
    IoUtil.closeQuietly(socket);
  }

  // TODO: reuse buffers?
  public void emitted(Map<String, Object>log)  {
    if (socket == null) return;

    ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream(1024);
    Writer messageWriter = null;
    try {
      messageWriter = new OutputStreamWriter(messageBuffer, "utf-8");
      JsonWriter jsonWriter = new JsonWriter(messageWriter);
      jsonWriter.write(log);
      messageWriter.write("\n");
    } catch (IOException e) {
      throw new InternalSpockError(e);
    } finally {
      IoUtil.closeQuietly(messageWriter);
    }

    ByteArrayOutputStream sizeBuffer = new ByteArrayOutputStream();
    Writer sizeWriter = null;
    try {
      sizeWriter = new OutputStreamWriter(sizeBuffer, "utf-8");
      sizeWriter.write(String.valueOf(messageBuffer.size()));
      sizeWriter.write("\n");
    } catch (IOException e) {
      throw new InternalSpockError(e);
    } finally {
      IoUtil.closeQuietly(sizeWriter);
    }

    try {
      socket.getOutputStream().write(sizeBuffer.toByteArray());
      socket.getOutputStream().write(messageBuffer.toByteArray());
      socket.getOutputStream().flush();
    } catch (Exception e) {
      IoUtil.closeQuietly(socket);
      socket = null;
      throw new ExtensionException("Error sending data to report server. " +
          "Server address: $reportServerAddress Server port: $reportServerPort", e);
    }
  }
}
