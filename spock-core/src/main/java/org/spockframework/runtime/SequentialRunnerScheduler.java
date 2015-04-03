package org.spockframework.runtime;

import org.junit.runners.model.RunnerScheduler;

public class SequentialRunnerScheduler implements RunnerScheduler{
    @Override
    public void schedule(Runnable childStatement) {
        childStatement.run();
    }

    @Override
    public void finished() {

    }
}
