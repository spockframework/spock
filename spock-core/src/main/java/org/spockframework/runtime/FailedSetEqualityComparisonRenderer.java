package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;

import java.util.*;

public class FailedSetEqualityComparisonRenderer implements ExpressionComparisonRenderer {
  @Override
  public String render(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!(expr.isEqualityComparison() || expr.isSetComparison())) return null;

    Object value1 = expr.getChildren().get(0).getValue();
    Object value2 = expr.getChildren().get(1).getValue();
    if (!(value1 instanceof Set) || !(value2 instanceof Set))
      return null;

    Set actual = (Set)value1;
    Set expected = (Set)value2;

    Set missing = new LinkedHashSet(expected);
    Set extra = new LinkedHashSet(actual);
    missing.removeAll(actual);
    extra.removeAll(expected);

    int missingSize = missing.size();
    int extraSize = extra.size();
    int maxDistance = Math.max(actual.size(), expected.size());
    int editDistance = Math.max(missingSize, extraSize);
    int similarityInPercent = (maxDistance - editDistance) * 100 / maxDistance;
    int differences = missingSize + extraSize;

    if (editDistance < 11)
      return String.format("false%n%d difference%s (%d%% similarity, %d missing, %d extra)%nmissing: %s%nextra: %s",
        differences,
        differences == 1 ? "" : "s",
        similarityInPercent,
        missingSize, extraSize,
        missing, extra);

    return String.format("false%n%d differences (%d%% similarity, %d missing, %d extra)%nmissing/extra: too many to render",
      differences,
      similarityInPercent,
      missingSize, extraSize);
  }
}
