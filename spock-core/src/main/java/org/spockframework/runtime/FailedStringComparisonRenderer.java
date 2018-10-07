package org.spockframework.runtime;

import org.spockframework.runtime.condition.*;
import org.spockframework.runtime.model.ExpressionInfo;

import groovy.lang.GString;

public class FailedStringComparisonRenderer implements ExpressionComparisonRenderer {
  public static final long MAX_EDIT_DISTANCE_MEMORY = 50 * 1024;
  @Override
  public String render(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!expr.isEqualityComparison(String.class, GString.class)) return null;

    // values can't be null here
    String str1 = expr.getChildren().get(0).getValue().toString();
    String str2 = expr.getChildren().get(1).getValue().toString();

    if (((long)str1.length()) * str2.length() > MAX_EDIT_DISTANCE_MEMORY) {
      return tryReduceStringSizes(str1, str2);
    } else {
      return createAndRenderEditDistance(str1, str2);
    }
  }

  private String tryReduceStringSizes(String str1, String str2) {
    int minLength = Math.min(str1.length(), str2.length());
    int commonStart = minLength;
    for (int i = 0; i < minLength; i++) {
      if (str1.charAt(i) != str2.charAt(i)) {
        commonStart = i-1;
        break;
      }
    }
    commonStart = Math.max(0, commonStart);
    int end1 = str1.length()-1;
    int end2 = str2.length()-1;
    while (end1 >= 0 && end2 >= 0 && str1.charAt(end1) == str2.charAt(end2)){
      end1--; end2--;
    }
    end1++;
    end2++;

    if (((long) end1-commonStart) * (end2-commonStart) > MAX_EDIT_DISTANCE_MEMORY) {
      return "false\nStrings too large to calculate edit distance.";
    } else {
      // Check if we can add some context
      if (((long) end1 - commonStart + 20) * (end2 - commonStart + 20) < MAX_EDIT_DISTANCE_MEMORY){
        commonStart = Math.max(0, commonStart - 10);
        end1 = Math.min(str1.length(), end1 + 10);
        end2 = Math.min(str2.length(), end2 + 10);
      }
      return createAndRenderEditDistance(str1, str2, commonStart, end1, end2);
    }
  }

  private String createAndRenderEditDistance(String str1, String str2) {
    EditDistance dist = new EditDistance(str1, str2);
    return String.format("false\n%d difference%s (%d%% similarity)\n%s",
      dist.getDistance(), dist.getDistance() == 1 ? "" : "s", dist.getSimilarityInPercent(),
      new EditPathRenderer().render(str1, str2, dist.calculatePath()));
  }

  private String createAndRenderEditDistance(String str1, String str2, int commonStart, int end1, int end2) {
    String sub1 = str1.substring(commonStart, end1);
    String sub2 = str2.substring(commonStart, end2);
    EditDistance dist = new EditDistance(sub1, sub2);
    return String.format("false\n%d difference%s (%d%% similarity) (comparing subset start: %d, end1: %d, end2: %d)\n%s",
      dist.getDistance(), dist.getDistance() == 1 ? "" : "s", dist.getSimilarityInPercent(),
      commonStart, end1, end2,
      new EditPathRenderer().render(sub1, sub2, dist.calculatePath()));
  }
}
