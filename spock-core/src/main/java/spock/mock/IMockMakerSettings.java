package spock.mock;

import groovy.lang.Closure;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.util.Beta;

/**
 * Settings passed to the {@link IMockMaker} and created explicitly for the requested mock maker.
 * The user will create such an instance at the mocking site:
 *
 * <p>{@code Mock(mockMaker: yourMockSettings { yourProperty = true }, ClassToMock)}
 *
 * <p>The {@link IMockMaker} should define a static constant or method (see {@link MockMakers}),
 * which let the user construct a custom {@link IMockMakerSettings}.
 *
 * <p>If you provide a method, you can define your settings in a typesafe manner for the user,
 * e.g. with a {@link Closure} parameter to configure the mock.
 *
 * @since 2.4
 */
@Beta
public interface IMockMakerSettings {

  /**
   * Creates a default {@code IMockMakerSettings} object with only an {@link MockMakerId}.
   *
   * @param id the mock maker ID.
   * @return the settings object
   */
  static IMockMakerSettings settingsFor(MockMakerId id) {
    return new IMockMakerSettings() {
      @Override
      public MockMakerId getMockMakerId() {
        return id;
      }

      @Override
      public String toString() {
        return id + " default mock maker settings";
      }
    };
  }

  MockMakerId getMockMakerId();
}
