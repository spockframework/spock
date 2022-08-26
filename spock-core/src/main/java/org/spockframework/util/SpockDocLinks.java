package org.spockframework.util;

public enum SpockDocLinks {
  SPY_ON_JAVA_17("instance-spy-on-java-17");

  private static final String SPOCK_DOCS_ROOT = "https://spockframework.org/spock/docs/";
  private static final String ALL_IN_ONE_DOC = "/all_in_one.html";
  private final String anchor;

  SpockDocLinks(String anchor) {
    this.anchor = anchor;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getLink() {
    return SPOCK_DOCS_ROOT + SpockReleaseInfo.getVersion().toOriginalString(false, true) + ALL_IN_ONE_DOC + "#" + anchor;
  }
}
