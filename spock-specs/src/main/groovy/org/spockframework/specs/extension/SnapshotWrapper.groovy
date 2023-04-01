package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

@CompileStatic
class SnapshotWrapper {
  static final SnapshotWrapper NOOP = new Noop()
  static final SnapshotWrapper SPEC_BODY = of(
    """\
package aPackage
import spock.lang.*

class ASpec extends Specification {""",
    "}"
  )
  static final SnapshotWrapper FEATURE_BODY = of(
    """\
package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {""",
    """\
  }
}
"""
  )

  private static final String UPPER_BARRIER = "\n/*--------- tag::snapshot[] ---------*/\n"
  private static final String LOWER_BARRIER = "\n/*--------- end::snapshot[] ---------*/\n"
  private final String prefix
  private final String suffix

  @PackageScope
  SnapshotWrapper(String prefix, String suffix) {
    this.prefix = prefix
    this.suffix = suffix
  }

  static SnapshotWrapper of(String prefix, String suffix) {
    new SnapshotWrapper(prefix, suffix)
  }

  @PackageScope
  String wrap(String value) {
    prefix + UPPER_BARRIER + value + LOWER_BARRIER + suffix
  }

  @PackageScope
  String unwrap(String value) {
    // check if unwrapping is possible
    if (!value.startsWith(prefix) || !value.endsWith(suffix)) {
      return value
    }
    value.substring(prefix.length() + UPPER_BARRIER.length(), value.length() - suffix.length() - LOWER_BARRIER.length())
  }

  final static class Noop extends SnapshotWrapper {

    private Noop() {
      super("", "")
    }

    @Override
    String wrap(String value) {
      value
    }

    @Override
    String unwrap(String value) {
      value
    }
  }
}
