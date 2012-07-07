package spock.mock;

import org.spockframework.mock.IMockObject;
import org.spockframework.mock.IMockObjectProvider;

import spock.lang.Beta;

@Beta
public class MockDetector {
  public static boolean isMockObject(Object object) {
    return object instanceof IMockObjectProvider;
  }

  public static IMockObject asMockObject(Object mock) {
    if (!isMockObject(mock)) {
      throw new IllegalArgumentException("Not a mock object: " + mock.toString());
    }

    IMockObjectProvider provider = (IMockObjectProvider) mock;
    return provider.$spock_get();
  }
}
