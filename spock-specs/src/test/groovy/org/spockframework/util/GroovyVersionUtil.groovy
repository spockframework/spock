package org.spockframework.util

class GroovyVersionUtil {

  static boolean isGroovy2() {
    return GroovySystem.getVersion().startsWith("2.")
  }
}
