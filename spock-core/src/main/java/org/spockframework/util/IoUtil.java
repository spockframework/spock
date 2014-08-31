/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.util;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IoUtil {
  public static void closeQuietly(@Nullable Closeable... closeables) {
    if (closeables == null) return;

    for (Closeable closeable : closeables) {
      if (closeable == null) return;

      try {
        closeable.close();
      } catch (IOException ignored) {}
    }
  }

  // In JDK 1.6, java.net.Socket doesn't implement Closeable, so we have this overload.
  public static void closeQuietly(@Nullable final Socket... closeables) {
    if (closeables == null) return;

    for (Closeable closeable : closeables) {
      if (closeable == null) return;

      try {
        closeable.close();
      } catch (IOException ignored) {}
    }
  }

  public static void stopQuietly(@Nullable IStoppable... stoppables) {
    if (stoppables == null) return;

    for (IStoppable stoppable : stoppables) {
      if (stoppable == null) return;

      try {
        stoppable.stop();
      } catch (Exception ignored) {}
    }
  }

  /**
   * Returns the text read from the given reader as a String.
   * Closes the given reader upon return.
   */
  public static String getText(Reader reader) throws IOException {
    try {
      StringBuilder source = new StringBuilder();
      BufferedReader buffered = new BufferedReader(reader);
      String line = buffered.readLine();

      while (line != null) {
        source.append(line);
        source.append('\n');
        line = buffered.readLine();
      }

      return source.toString();
    } finally {
      closeQuietly(reader);
    }
  }

  /**
   * Returns the text read from the given file as a String.
   */
  public static String getText(File path) throws IOException {
    return getText(new FileReader(path));
  }

  /**
   * Returns the text read from the given stream as a String.
   * Closes the given stream upon return.
   */
  public static String getText(InputStream stream) throws IOException {
    return getText(new InputStreamReader(stream));
  }

  public static void createDirectory(File dir) throws IOException {
    if (!dir.isDirectory() && !dir.mkdirs())
      throw new IOException("Failed to create directory: " + dir);
  }

  public static void copyStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];

    try {
      int read = in.read(buffer);
      while (read > 0) {
        out.write(buffer, 0, read);
        read = in.read(buffer);
      }
    } finally {
      IoUtil.closeQuietly(in, out);
    }
  }

  public static void copyFile(File source, File target) throws FileNotFoundException, IOException {
    if (!source.isFile())
      throw new IOException(String.format("Error copying file %s to %s; source file does not exist", source, target));

    createDirectory(target.getParentFile());
    doCopyFile(source, target);
    checkSameSize(source, target);
  }

  public static List<File> listFilesRecursively(File baseDir) throws IOException {
    if (!baseDir.exists()) return Collections.emptyList();

    List<File> result = new ArrayList<File>();
    doListFilesRecursively(baseDir, result);
    return result;
  }

  @Nullable
  public static String getFileExtension(String filename) {
    int index = filename.lastIndexOf('.');
    return index == -1 ? null : filename.substring(index + 1, filename.length());
  }

  private static void doListFilesRecursively(File baseDir, List<File> result) throws IOException {
    List<File> dirs = new ArrayList<File>();
    File[] files = baseDir.listFiles();
    if (files == null) {
      throw new IOException("Failed to list files of directory '" + baseDir + "'");
    }
    for (File file: files) {
      if (file.isFile()) {
        result.add(file);
      } else {
        dirs.add(file);
      }
    }
    for (File dir: dirs) {
      doListFilesRecursively(dir, result);
    }
  }

  private static void doCopyFile(File source, File target) throws IOException {
    FileInputStream in = null;
    FileOutputStream out = null;

    try {
      in = new FileInputStream(source);
      out = new FileOutputStream(target);
      // instead of checking transferred size, we'll compare file sizes in checkSameSize()
      in.getChannel().transferTo(0, Long.MAX_VALUE, out.getChannel());
    } finally {
      IoUtil.closeQuietly(in, out);
    }
  }

  private static void checkSameSize(File source, File target) throws IOException {
    long fromSize = source.length();
    long toSize = target.length();

    if (fromSize != toSize)
      throw new IOException(String.format(
          "Error copying file %s to %s; source file size is %d, but target file size is %d",
          source, target, fromSize, toSize));
  }
}
