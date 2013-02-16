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

import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.util.IoUtil
import org.spockframework.util.JsonWriter

class ReportServerClient implements IReportLogListener {
  private final String reportServerAddress
  private final int reportServerPort

  private Socket socket

  ReportServerClient(String reportServerAddress, int reportServerPort) {
    this.reportServerAddress = reportServerAddress
    this.reportServerPort = reportServerPort
  }

  void start() {
    socket = new Socket(reportServerAddress, reportServerPort)
  }

  void stop() {
    IoUtil.closeQuietly(socket)
  }

  // TODO: reuse buffers?
  void emitted(Map log) {
    if (socket == null) return

    def messageBuffer = new ByteArrayOutputStream(1024)
    messageBuffer.withWriter("utf-8") { writer ->
      def jsonWriter = new JsonWriter(writer)
      jsonWriter.write(log)
      writer.write("\n")
    }

    def sizeBuffer = new ByteArrayOutputStream()
    sizeBuffer.withWriter("utf-8") { writer ->
      writer.write(String.valueOf(messageBuffer.size()))
      writer.write("\n")
    }

    try {
      socket.outputStream.write(sizeBuffer.toByteArray())
      socket.outputStream.write(messageBuffer.toByteArray())
      socket.outputStream.flush()
    } catch (Exception e) {
      IoUtil.closeQuietly(socket)
      socket = null
      throw new ExtensionException("Error sending data to report server. " +
          "Server address: $reportServerAddress Server port: $reportServerPort", e)
    }
  }
}
