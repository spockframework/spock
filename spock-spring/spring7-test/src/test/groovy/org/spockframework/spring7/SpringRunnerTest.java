package org.spockframework.spring7;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Make sure we still can correctly execute tests executed by the SpringRunner that are not spock tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SpringRunnerTest {

  @Test
  public void testThatSpringRunnerExecutesCorrectly() {
    Assert.assertTrue(true);
  }

}
