package org.spockframework.junit4.junit

import spock.lang.Specification
import spock.util.EmbeddedSpecCompiler
import spock.util.EmbeddedSpecRunner

class JUnitBaseSpec extends Specification {
  EmbeddedSpecRunner runner = new EmbeddedSpecRunner()
  EmbeddedSpecCompiler compiler = new EmbeddedSpecCompiler()
}
