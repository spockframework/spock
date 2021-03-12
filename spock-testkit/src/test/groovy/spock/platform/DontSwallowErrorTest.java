package spock.platform;

import spock.testkit.oom.OomSpec;

import org.junit.jupiter.api.*;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class DontSwallowErrorTest extends SpockEngineBase {

  @Test
  void verifyUnrollExample() {
    Assertions.assertThrows(OutOfMemoryError.class,
      () -> execute(selectClass(OomSpec.class), stats -> stats.started(1)));
  }
}
