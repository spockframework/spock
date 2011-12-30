package org.spockframework.util;

import java.util.Arrays;
import java.util.List;

public class ParseUtil {
  public static Boolean parseBoolean(String string) {
    try {
      return Boolean.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  public static Byte parseByte(String string) {
    try {
      return Byte.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Short parseShort(String string) {
    try {
      return Short.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Integer parseInt(String string) {
    try {
      return Integer.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Long parseLong(String string) {
    try {
      return Long.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Float parseFloat(String string) {
    try {
      return Float.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Double parseDouble(String string) {
    try {
      return Double.valueOf(string);
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  public static List<String> parseList(String string) {
    return parseList(string, ",");
  }

  public static List<String> parseList(String string, String separator) {
    return Arrays.asList(string.split(separator));  
  }
}
