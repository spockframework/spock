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
    byte[] buffer = new byte[4096];

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
