package org.spockframework.runtime;

public class AsyncStandardStreamsListener extends AsyncRunListener implements IStandardStreamsListener {
  private final IStandardStreamsListener streamsDelegate;

  public AsyncStandardStreamsListener(String threadName, IRunListener delegate, IStandardStreamsListener streamsDelegate) {
    super(threadName, delegate);
    this.streamsDelegate = streamsDelegate;
  }

  public void standardOut(final String message) {
    addEvent(new Runnable() {
      public void run() {
        streamsDelegate.standardOut(message);
      }
    });
  }

  public void standardErr(final String message) {
    addEvent(new Runnable() {
      public void run() {
        streamsDelegate.standardErr(message);
      }
    });
  }
}
