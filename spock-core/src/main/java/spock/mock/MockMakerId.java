package spock.mock;

import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.util.Beta;
import org.spockframework.util.Checks;
import org.spockframework.util.Immutable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents the ID of an {@link IMockMaker}.
 *
 * @since 2.4
 */
@Immutable
@Beta
public final class MockMakerId implements Comparable<MockMakerId> {
  private static final Pattern VALID_ID = Pattern.compile("^[a-z][a-z0-9-]*+(?<!-)$");
  private final String id;

  public MockMakerId(String id) {
    Checks.notNull(id, () -> "The ID is null.");
    Checks.checkArgument(VALID_ID.matcher(id).matches(), () ->
"The ID '" + id + "' is invalid. A valid ID must comply with the pattern: " + VALID_ID);

    this.id = id;
  }

  @Override
  public int compareTo(MockMakerId o) {
    return this.id.compareTo(o.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MockMakerId that = (MockMakerId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id;
  }
}
