package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * A tag to for use in {@link ITestTaggable}.
 *
 * @since 2.2
 * @author Leonard Brünings
 */
public class TestTag {

  /*
   * Should filter the same characters as JUnit Platforms test tag.
   */
  private static final IntPredicate INVALID_CODE_POINTS = Stream.of(
    Character::isWhitespace,
    Character::isISOControl,
    isReservedChar()
  ).reduce(IntPredicate::or).get();
  private static final String RESERVED_CHARACTERS = "!&(),|";
  private final String value;

  private TestTag(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Creates a new {@link TestTag} with the given value.
   *
   * Must not contain any of the following characters:
   * <ul>
   *   <li>White Space</li>
   *   <li>ISO Control Characters</li>
   *   <li>{@code !}</li>
   *   <li>{@code &}</li>
   *   <li>{@code (}</li>
   *   <li>{@code )}</li>
   *   <li>{@code ,}</li>
   *   <li>{@code |}</li>
   * </ul>
   *
   * @param tag the value of the tag
   * @return a new {@link TestTag}
   */
  public static TestTag create(String tag) {
    if(!isValid(tag)){
      throw new IllegalArgumentException("Invalid tag: [" + tag + "] contains invalid character(s): " + getInvalidCharacters(tag));
    }
    return new TestTag(tag);
  }


  public static boolean isValid(@Nullable String value) {
    return value != null && value.length() > 0 && value.codePoints().noneMatch(INVALID_CODE_POINTS);
  }

  private static String getInvalidCharacters(@Nullable String value) {
    if (value == null) {
      return "tag is null";
    }
    if (value.isEmpty()) {
      return "tag is empty";
    }
    return value.codePoints()
      .filter(INVALID_CODE_POINTS)
      .distinct()
      .mapToObj(Character::getName)
      .collect(joining(", "));
  }

  private static IntPredicate isReservedChar(){
    return RESERVED_CHARACTERS.codePoints()
      .mapToObj(codePoint -> (IntPredicate) other -> codePoint == other)
      .reduce(IntPredicate::or)
      .orElseThrow(() -> new IllegalStateException("This should never happen (tm)"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TestTag testTag = (TestTag) o;
    return value.equals(testTag.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
