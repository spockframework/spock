package org.spockframework.junit

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description


class JUnitRuleBehavior extends Base {
  boolean started = false

  @Rule public TestRule rule = new TestWatcher() {
    @Override
    protected void starting(Description description) {
      started = true
    }
  }

  abstract static class Base {
    @Test
    void ruleInDerivedClassAffectsTestInBaseClass() {
      assert this.started
    }
  }
}


