package org.spockframework.util;

import java.io.*;

public class IoUtil {
  public static void closeQuietly(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException ignored) {}
  }

  public static String getText(File path) throws IOException {
    StringBuilder source = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(path));
      String line = reader.readLine();
      while (line != null) {
        source.append(line);
        source.append('\n');
        line = reader.readLine();
      }
    } finally {
      closeQuietly(reader);
    }

    return source.toString();
  }
}
