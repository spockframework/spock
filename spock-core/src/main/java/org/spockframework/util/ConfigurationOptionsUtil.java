package org.spockframework.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationOptionsUtil {
  private static final Pattern LOWER_UPPER = Pattern.compile("([^\\p{Upper}]*)(\\p{Upper}*)");

  public static String camelCaseToConstantCase(String value) {
    if (value == null || value.equals("")) return value;

    StringBuilder result = new StringBuilder();
    Matcher matcher = LOWER_UPPER.matcher(value);

    while (matcher.find()) {
      String lowers = matcher.group(1);
      String uppers = matcher.group(2);

      if (uppers.length() == 0) {
        result.append(lowers.toUpperCase());
      } else {
        if (lowers.length() > 0) {
          result.append(lowers.toUpperCase());
          result.append('_');
        }
        if (uppers.length() > 1 && !matcher.hitEnd()) {
          result.append(uppers.substring(0, uppers.length() - 1));
          result.append('_');
          result.append(uppers.charAt(uppers.length() - 1));
        } else {
          result.append(uppers);
        }
      }
    }

    return result.toString();
  }
}
