package org.spockframework.util

import spock.lang.Specification

class ConfigurationOptionsUtilSpec extends Specification {
  def "camelCaseToConstantCase"() {
    expect:
    ConfigurationOptionsUtil.camelCaseToConstantCase(camelCase) == constantCase

    where:
    camelCase      | constantCase
    ""             | ""
    "do"           | "DO"
    "DO"           | "DO"
    "doIt"         | "DO_IT"
    "DoIt"         | "DO_IT"
    "doIT"         | "DO_IT"
    "DOit"         | "D_OIT"
    "useTCPStack"  | "USE_TCP_STACK"
    "useTCPStacK"  | "USE_TCP_STAC_K"
    "fred123Usage" | "FRED123_USAGE"
  }
}
